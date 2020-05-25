在AOP方案时，我们提到了可以在某些函数前后通过JavaAssit的方式来添加相关的代码，那么能不能要是进一步的修改字节码，
如修改某些已有的jar的文件class或者整体修、插入很多相似的代码，有什么比较好的方案么，若是JavaAssit搞不定的就可以考虑ASM。

若是你不了解ASM，没关系，可以先来个demo，来看下ASM能干什么，我们将通过下面的三个小案例来大家去了解它。

# 案例一 生成字节码

我们可以来看下下面的代码片段：
```java
/**
* 自定义各个classLoader
/
class MyClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
        // ClassLoader是个抽象类，而ClassLoader.defineClass 方法是protected的
        // 所以我们需要定义一个子类将这个方法暴露出来
        return super.defineClass(name, b, 0, b.length);
    }
}


private static byte[] generate() {
        ClassWriter cw = new ClassWriter(0);
        // 定义对象头：版本号、修饰符、全类名、签名、父类、实现的接口
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "cc/cyning/asm/HelloWorld",
                null, "java/lang/Object", null);


        // 添加方法：修饰符、方法名、描述符、签名、抛出的异常
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
                "([Ljava/lang/String;)V", null, null);
        // 执行指令：获取静态属性
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // 加载常量 load constant
        mv.visitLdcInsn("Hello  World ASM!");
        // 调用方法
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        // 返回
        mv.visitInsn(Opcodes.RETURN);
        // 设置栈大小和局部变量表大小
        mv.visitMaxs(2, 1);
        // 方法结束
        mv.visitEnd();

        // 类完成
        cw.visitEnd();
        // 生成字节数组
        return cw.toByteArray();
}

```


这样通过generate函数就会生成字节码，这个字节码就可以load到自定义的classLoader中，看下结果

```java
        // 通过ASM生成二进制字节码
        byte[] bytes = generate();
        // 使用自定义的ClassLoader
        MyClassLoader cl = new MyClassLoader();
        // 加载我们生成的 HelloWorld 类
        Class<?> clazz = cl.defineClass("cc.cyning.asm.HelloWorld", bytes);
        // 反射获取 main 方法
        Method main = clazz.getMethod("main", String[].class);
        // 调用 main 方法
        main.invoke(null, new Object[]{new String[]{}});
```
![](https://raw.githubusercontent.com/ownwell/image-bed/master/img/2020-05-22-23-10-15.png)
若是你直接将这个生成的字节码存放到一个class中，器结果如下：
```java
package cc.cyning.asm;

public class HelloWorld {
    public static void main(String[] var0) {
        System.out.println("Hello  World ASM!");
    }
}
```
是不是特别有趣的已将事情呢？ 有人要问，generate函数代表什么啊，这个你若是看过class文件，就可能会跟了解。

我就直接用javap将class的信息打印出来如下：
![](https://raw.githubusercontent.com/ownwell/image-bed/master/img/java.png)
这个就一目了然，先通过`GETSTATIC`指令拿到`java/lang/System`的`out`属性，再加载常量`Hello  World ASM!`,而后通过`INVOKEVIRTUAL`指令调用out的println方法，参数就是刚才的常量。

这样一步步写，太过晦涩，可以直接在idea或者Android Studio上安装一个`ASM Bytecode Outline`
,后面会用我就不再介绍了。


## 替换后写函数的调用
记得在android7.1.1（android 25）时有个Toast.show就有badTokenException的异常，之前[修复方案](https://www.jianshu.com/p/3d94f8a6ec0)在为hook TN类的变量mHandler，在调用`handleMessage`能及时捕获到异常，这个方案就不再啰嗦。
那么我们如何来解决我们的问题呢，很简单，就是直接替换：
```java
 public static FixToast makeText(Context context, CharSequence text, int duration) {
        toast = Toast.makeText(context,  text, duration);
        return new FixToast();
    }

    public void show() {
        if (toast == null) {
            throw new RuntimeException("请先调用makeText方法");
        }
        if (Build.VERSION.SDK_INT == 25) {
            workaround(toast).show();
        } else {
            toast.show();
        }
    }
```
将所有调用Toast未加保护的地方全部替换`FixToast`,一个个替换，真有可能会类似，若是有位同学不知道这个问题，有写成了`Toast`,那可怎么办，祭出我们打杀器ASM。

ASM能够获取到所有的函数调用关系，其中函数调用（visitMethodInsn）中，有个变量格外引人注目
![](https://raw.githubusercontent.com/ownwell/image-bed/master/img/2020-05-23-00-14-44.png)

owner表示调用的类，如：
```java
A  a = new A();
a.show()
```
其中函数`show`的owner就是`A`类,所以使用ASM就可以直接修改：
```java

    public static final String  TOAST_CLASS = "android/widget/Toast";
    public static final String   FIX_TOAST_CLASS = "cc/cyning/fixtoast/FixToast";

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
         //覆盖方法调用的过程
            if (opcode == Opcodes.INVOKESTATIC && TOAST_CLASS.equals(owner) ) {

                desc = "(Landroid/content/Context;Ljava/lang/CharSequence;I)L"+FIX_TOAST_CLASS+";";
                //方法描述符替换为影子方法
                owner = FIX_TOAST_CLASS;
                System.out.println("dfdfdfdfd >>> " + desc);
            }
            if(name.equals( "show") && TOAST_CLASS.equals(owner)) {
                owner = FIX_TOAST_CLASS;
            }
    }
```

## 快速点击的问题

在我们开发中，有时候快速输入或点击会操作结果的不可期，如EditText作为搜索框，你输入过快，会出现不必要的搜索请求，还有常用的点击事件，怎么才能规避这个问题呢？
Rxjava中有`throttle`放抖动的操作符，但是有没有使用ASM无侵入的改动呢？
当然可以,这就需要催一下一个工具：
`ASM Bytecode Outline`
### `ASM Bytecode Outline`
1. 选择你要生成或修改的java文件
2. 生成后，在ASMified车侧栏下找到对应的代码

### 检查是否快速点击
我们可以先写一段代码：
```java
  View mView = null;

        if (FastClick.isFastDoubleClick(mView, 100)) {
            System.out.println("hello world");
        }
```
生成的核心代码：
```java
mv.visitLineNumber(8, l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(new Long(100L));
            mv.visitMethodInsn(
                    INVOKESTATIC, "cc/cyning/fastclick/FastClick", "isFastDoubleClick",
                    "(Landroid/view/View;J)Z", false
            );
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(9, l2);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("hello world");
            mv.visitMethodInsn(
                    INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
                    false
            );
```
1. 将view和100load到操作数栈
2. 执行FastClick.isFastDoubleClick这个静态方法
3. 若是IFEQ（返回的true） 就执行原有代码


好了，直接放到代码里：
```java

    @Override
    public void visitCode() {
        super.visitCode();
        p("visitCode " + nameDesc);
        if (nameDesc.equals("onClick(Landroid/view/View;)V") && "true".equals(mIdValue)) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(new Long(800L));
            mv.visitMethodInsn(
                    INVOKESTATIC, "cc/cyning/fastclick/FastClick", "isFastDoubleClick",
                    "(Landroid/view/View;J)Z", false
            );

            Label label = new Label();
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(RETURN);
            mv.visitLabel(label);

        }
```
运行下看下结果吧：

```java
public void onClick(View var1) {
                if (!FastClick.isFastDoubleClick(var1, 800L)) {
                    try {
                        Thread.sleep(800L);// 26
                    } catch (InterruptedException var3) {// 27
                        var3.printStackTrace();// 28
                    }

                    Log.d("MainActivity", "onClick: ");// 30
                }
            }// 31
```
>> 注释的后面的数字是行号，ASM不会改变行号

## 小节

1. ASM目前可以自己生成相关的class
2. 能修改部分代码（如变量，调用关系）
3. 可以在函数执行的某个切面添加或者修改
其实他还有更强大的功能，结合Gradle插件的开发，会激发他的最大潜力。

项目地址：[https://github.com/ownwell/Gradle-plugin](https://github.com/ownwell/Gradle-plugin)