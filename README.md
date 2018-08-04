# EclipseLoading

🌞 日食加载动画

设计来自[1982200-Solar-Eclipse](https://dribbble.com/shots/1982200-Solar-Eclipse)

## 预览

![eclipseloading.gif](https://github.com/Bakumon/EclipseLoading/raw/master/gif/eclipseloading.gif)

## 思路

~~**日食思路1：** 先画好太阳，并在太阳右边紧贴着画一个和太阳一样大的白色圆，然后使用属性动画把白色圆往左移动，逐渐覆盖在太阳上，就形成了日食动画。~~

但是这个思路两个弊端：**第一**，白色圆的颜色必须和背景色一样。**第二**，白色圆和背景重复绘制了相同的区域。

**日食思路2：** 先画好太阳，用一个和太阳一样大圆形的 path 对太阳进行裁剪（裁剪掉 path 内的部分），然后通过属性动画向左移动 path 达到日食的效果。并且同时避免了上面两个问题。

## 使用

```xml
<me.bakumon.library.EclipseLoadingView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

**设置太阳颜色**

1. 自定义属性

```xml
app:sunColor="@color/colorAccent"
```

2. java 代码

```java
eclipseLoadingView.setSunColor(sunColor);
```
