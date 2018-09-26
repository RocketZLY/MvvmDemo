# 关于Android MvvM的一些体会

### 前言

> 由于我司项目较老有很多历史包袱代码结构也比较混乱，需求复杂的页面动辄activity中1000多行，看着很是头疼，于是趁着加班提前做完需求余下的时间学习了mvvm对项目部分功能进行了改造，目前已经使用3个版本了，本篇博文分享下我使用的感受。

### 准备

> 这里先说说关于mvvm的几个问题（如有不对请轻喷 (*╹▽╹*)）

1. 首先说说我为啥选择mvvm而不是熟知的mvp。

   主要原因是我觉得mvp接口写起来有点麻烦，针对ui和model都得写接口，然后这个粒度不好控制如果太细了就得写一堆接口，太粗了又没有复用性，并且presenter持有了ui引用在更新ui的时候还得考虑生命周期，还有activity引用的处理防止内存泄露这些问题我都觉得挺麻烦的而MvvM中databinding框架处理好了这些问题，所以我选择了更加方便的mvvm，当然mvvm也不是没有缺点下面会说到。

2. mvvm优缺点 
   - 优点:
     1. 数据源被强化，利用databinding框架实现双向绑定技术，当数据变化的时候ui自动更新，ui上用户操作数据自动更新，很好的做到数据的一致性。
     2. xml和activity处理ui操作、model提供数据、vm处理业务逻辑，各个层级分工明确，activity中代码大大减少项目整体结构更加清晰。
     3. 很方便做ui的a/b测试可以共用同一个vm。
     4. 方便单元测试ui和vm逻辑完全分离。
   - 缺点:
     1. bug很难被调试，数据绑定使得一个bug被传递到别的位置，要找到bug的原始位置不太容易。
     2. 由于要遵守模式的规范调用流程变得复杂。
     3. vm中会有很多被观察者变量如果业务逻辑非常复杂会消耗更多内存。

3. mvvm一定要用databinding么？

   答案是 否。首先我们要了解到mvvm是数据驱动的架构，所以着眼点是数据的变化，那么我们需要实现一套ui和数据双向绑定的逻辑，当数据修改的时候通知ui改变，ui输入或者点击的时候触发数据修改，而databinding就是帮你实现这个双向绑定过程的框架，在xml中按它的语法去写布局，然后他会根据你在xml中所写的生成对应的类帮你实现这个绑定过程，当然你也可以自己手动实现这个绑定过程，所以databinding是非必须的。

### 项目结构图

![](http://of1ktyksz.bkt.clouddn.com/mvvm_architecture.png)

上面是mvvm基本的结构图，act/fra和xml是v处理ui操作、viewmodel是vm处理业务逻辑、repository是m提供数据，他们之间是一种单项的持有关系activity/fragment持有vm，vm持有model。

对于Repository不太理解的可以看看这篇文章[Repository模式](https://github.com/wecodexyz/Componentization/wiki/Repository%E6%A8%A1%E5%BC%8F)

### 实际使用

项目中我使用的是retrofit+rxjava+livedata+viewmodel+databinding+kotlin实现的mvvm

retrofit+rejava用来在model层从网络获取数据通知到vm

livedata是vm通知ui时使用可以感知生命周期防止内存泄漏npe问题（主要用在事件传递上）

viewmodel是vm可以在act/frg因配置修改销毁的情况下复用

databinding实现ui和vm的双向绑定

这里来个具体例子，activity可见通知vm获取数据，vm从model拿到数据然后更新被观察者，ui自动刷新的流程。

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <variable
            name="vm"
            type="rocketly.mvvmdemo.viewmodel.HotCityListVM" />
    </data>

    <android.support.v4.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/srl"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:onRefreshListener="@{()->vm.onRefresh()}"//刷新自动触发vm.onRefresh()方法
        tools:context="rocketly.mvvmdemo.ui.MainActivity">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:multiTypeItem="@{vm.cityList}"//这里rv与vm中cityList绑定 />

    </android.support.v4.widget.SwipeRefreshLayout>
</layout>
```

xml中Recyclerview和vm的cityList绑定

```kotlin
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            vm = ViewModelProviders.of(this@MainActivity).get(HotCityListVM::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.vm?.onFirstLoad()//onResume调用vm.onFirstLoad()加载数据
    }

}
```

activity在onResume通知vm加载数据

```kotlin
class HotCityListVM : BaseVM() {
    val cityList = ObservableArrayList<Basic>()
    val hotCityItemEvent = SingleLiveEvent<String>()

    override fun onFirstLoad() {
        super.onFirstLoad()
        load()
    }

    override fun onRefresh() {
        super.onRefresh()
        load()
    }

    private fun load() {
        CityRepository.getHotCityList(num = 50)
                .subscribe(ApiObserver(success = {
                    resetLoadStatus()
                    cityList.clear()
                    cityList.addAll(it.HeWeather6[0].basic)
                }, error = {
                    resetLoadStatus()
                }))
    }

    fun hotCityItemClick(s: String) {
        hotCityItemEvent.value = s
    }
}
```

vm从model CityRepository获取数据修改被观察者对象cityList，然后ui监听到数据修改执行recyclerview刷新，这一套流程就走完了，具体例子在[MvvmDeno](https://github.com/RocketZLY/MvvmDemo)。



除了正常的请求数据显示逻辑，这里再演示下点击事件的流程，弹dialog或者其他需要context的事件也是同样方式。

recyclerview中item点击事件传递到vm然后vm通知activty执行对应的逻辑。

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>

        <variable
            name="vm"
            type="rocketly.mvvmdemo.viewmodel.HotCityListVM" />

        <variable
            name="data"
            type="rocketly.mvvmdemo.model.Basic" />
    </data>

    <android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:onClick="@{()->vm.hotCityItemClick(data.location)}"//调用vm的方法通知点击了>

        <TextView
            android:id="@+id/tv_city_name"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{data.location}"
            android:textColor="@android:color/black"
            android:textSize="18sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintHorizontal_chainStyle="packed"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toLeftOf="@+id/tv_lon"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="上海" />

        <TextView
            android:id="@+id/tv_lon"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginLeft="5dp"
            android:text="@{data.lon}"
            android:textColor="@android:color/black"
            android:textSize="14sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toRightOf="@+id/tv_city_name"
            app:layout_constraintRight_toLeftOf="@+id/tv_lat"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="(经度:555" />

        <TextView
            android:id="@+id/tv_lat"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginLeft="2dp"
            android:text="@{data.lat}"
            android:textColor="@android:color/black"
            android:textSize="14sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toRightOf="@+id/tv_lon"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="纬度:555)" />

    </android.support.constraint.ConstraintLayout>
</layout>
```

```kotlin
class HotCityListVM : BaseVM() {
    val hotCityItemEvent = SingleLiveEvent<String>()//给activty监听的被观察者livedata对象
    fun hotCityItemClick(s: String) {//点击方法
        hotCityItemEvent.value = s
    }
}
```

```kotlin
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            vm = ViewModelProviders.of(this@MainActivity).get(HotCityListVM::class.java)
        }
        initListener()
    }

    private fun initListener() {
        binding.vm?.apply {
            hotCityItemEvent.observe(this@MainActivity) {//监听item点击事件
                it ?: return@observe
                Toast.makeText(this@MainActivity, "点击了:$it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

以前我们都是在item中直接执行点击事件的，但为了遵守mvvm的规范，逻辑都在vm处理又因为vm不能持有context所以需要context的事件在通过livedata传递到activity执行。

那么一般的数据请求和点击事件的流程就讲完了，接下来说说databinding原理。

### DataBinding源码浅析

通过前面的例子可以发现对于databinding的使用一般可以分为如下几步

1. 在xml中按databinding的语法书写布局
2. activity中使用`DataBindingUtil.setContentView()`绑定View并获取binding对象
3. 把xml中声明的变量通过第二步得到的binding对象设置进去

#### 第一步按databinding语法书写xml，然后声明了一个变量vm，并将rv与vm的cityList绑定，刷新监听与vm的onRefresh方法绑定

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <variable
            name="vm"
            type="rocketly.mvvmdemo.viewmodel.HotCityListVM" />
    </data>

    <android.support.v4.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/srl"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:onRefreshListener="@{()->vm.onRefresh()}"
        tools:context="rocketly.mvvmdemo.ui.MainActivity">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:multiTypeItem="@{vm.cityList}" />

    </android.support.v4.widget.SwipeRefreshLayout>
</layout>

```

然后make project会生成一个布局名称+Binding的类，生成的路径如下

![](http://of1ktyksz.bkt.clouddn.com/mvvm_generate_class.png)

这个类就是按我们xml中所写生成的，这里把完整的生成类贴出来可以大概的看下。

```java
public class ActivityMainBinding extends android.databinding.ViewDataBinding implements android.databinding.generated.callback.OnRefreshListener.Listener {

    @Nullable
    private static final android.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = null;
    }
    // views
    @NonNull
    public final android.support.v7.widget.RecyclerView rv;
    @NonNull
    public final android.support.v4.widget.SwipeRefreshLayout srl;
    // variables
    @Nullable
    private rocketly.mvvmdemo.viewmodel.HotCityListVM mVm;
    @Nullable
    private final android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener mCallback2;
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBinding(@NonNull android.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        super(bindingComponent, root, 1);
        final Object[] bindings = mapBindings(bindingComponent, root, 2, sIncludes, sViewsWithIds);
        this.rv = (android.support.v7.widget.RecyclerView) bindings[1];
        this.rv.setTag(null);
        this.srl = (android.support.v4.widget.SwipeRefreshLayout) bindings[0];
        this.srl.setTag(null);
        setRootTag(root);
        // listeners
        mCallback2 = new android.databinding.generated.callback.OnRefreshListener(this, 1);
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x4L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
        if (BR.vm == variableId) {
            setVm((rocketly.mvvmdemo.viewmodel.HotCityListVM) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setVm(@Nullable rocketly.mvvmdemo.viewmodel.HotCityListVM Vm) {//根据我们xml中声明的vm变量生成的set方法
        this.mVm = Vm;
        synchronized(this) {
            mDirtyFlags |= 0x2L;
        }
        notifyPropertyChanged(BR.vm);
        super.requestRebind();
    }
    @Nullable
    public rocketly.mvvmdemo.viewmodel.HotCityListVM getVm() {
        return mVm;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangeVmCityList((android.databinding.ObservableArrayList<rocketly.mvvmdemo.model.Basic>) object, fieldId);
        }
        return false;
    }
    private boolean onChangeVmCityList(android.databinding.ObservableArrayList<rocketly.mvvmdemo.model.Basic> VmCityList, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        rocketly.mvvmdemo.viewmodel.HotCityListVM vm = mVm;
        android.databinding.ObservableArrayList<rocketly.mvvmdemo.model.Basic> vmCityList = null;

        if ((dirtyFlags & 0x7L) != 0) {



                if (vm != null) {
                    // read vm.cityList
                    vmCityList = vm.getCityList();
                }
                updateRegistration(0, vmCityList);
        }
        // batch finished
        if ((dirtyFlags & 0x7L) != 0) {
            // api target 1

            rocketly.mvvmdemo.utils.databinding.DataBindingExKt.setItem(this.rv, vmCityList);
        }
        if ((dirtyFlags & 0x4L) != 0) {
            // api target 1

            this.srl.setOnRefreshListener(mCallback2);
        }
    }
    // Listener Stub Implementations
    // callback impls
    public final void _internalCallbackOnRefresh(int sourceId ) {
        // localize variables for thread safety
        // vm != null
        boolean vmJavaLangObjectNull = false;
        // vm
        rocketly.mvvmdemo.viewmodel.HotCityListVM vm = mVm;



        vmJavaLangObjectNull = (vm) != (null);
        if (vmJavaLangObjectNull) {


            vm.onRefresh();
        }
    }
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;

    @NonNull
    public static ActivityMainBinding inflate(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    @NonNull
    public static ActivityMainBinding inflate(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup root, boolean attachToRoot, @Nullable android.databinding.DataBindingComponent bindingComponent) {
        return android.databinding.DataBindingUtil.<ActivityMainBinding>inflate(inflater, rocketly.mvvmdemo.R.layout.activity_main, root, attachToRoot, bindingComponent);
    }
    @NonNull
    public static ActivityMainBinding inflate(@NonNull android.view.LayoutInflater inflater) {
        return inflate(inflater, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    @NonNull
    public static ActivityMainBinding inflate(@NonNull android.view.LayoutInflater inflater, @Nullable android.databinding.DataBindingComponent bindingComponent) {
        return bind(inflater.inflate(rocketly.mvvmdemo.R.layout.activity_main, null, false), bindingComponent);
    }
    @NonNull
    public static ActivityMainBinding bind(@NonNull android.view.View view) {
        return bind(view, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    @NonNull
    public static ActivityMainBinding bind(@NonNull android.view.View view, @Nullable android.databinding.DataBindingComponent bindingComponent) {
        if (!"layout/activity_main_0".equals(view.getTag())) {
            throw new RuntimeException("view tag isn't correct on view:" + view.getTag());
        }
        return new ActivityMainBinding(bindingComponent, view);
    }
    /* flag mapping
        flag 0 (0x1L): vm.cityList
        flag 1 (0x2L): vm
        flag 2 (0x3L): null
    flag mapping end*/
    //end
}
```

我们一般需要用到的方法大致可以分为两类

1. 将生成的类与View绑定的bind方法和inflate方法（每个生成的binding类都会有）
2. 给我们set我们在xml声明变量的方法（这里是setVm方法）

#### 第二步在activity中通过`DataBindingUtil.setContentView()`方法将Binding类与View绑定

```ko
javaclass MainActivity : AppCompatActivity() {
...

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            vm = ViewModelProviders.of(this@MainActivity).get(HotCityListVM::class.java)
        }
        
    }
    
...
}
```

`DataBindingUtil.setContentView()`最后调用如下

```java
    @Override
    public android.databinding.ViewDataBinding getDataBinder(android.databinding.DataBindingComponent bindingComponent, android.view.View view, int layoutId) {
        switch (layoutId) {
            case rocketly.mvvmdemo.R.layout.activity_main: {
                final Object tag = view.getTag();
                if (tag == null) throw new java.lang.RuntimeException("view must have a tag");
                if ("layout/activity_main_0".equals(tag)) {
                    return new rocketly.mvvmdemo.databinding.ActivityMainBinding(bindingComponent, view);//创建ActivityMainBinding
                }
                throw new java.lang.IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
            }
        }
        return null;
    }
```

实际上就是new了一个ActivityMainBinding对象，那我们在看看它的构造方法

```java
    public ActivityMainBinding(@NonNull android.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        super(bindingComponent, root, 1);
        final Object[] bindings = mapBindings(bindingComponent, root, 2, sIncludes, sViewsWithIds);
        this.rv = (android.support.v7.widget.RecyclerView) bindings[1];
        this.rv.setTag(null);
        this.srl = (android.support.v4.widget.SwipeRefreshLayout) bindings[0];
        this.srl.setTag(null);
        setRootTag(root);
        // listeners
        mCallback2 = new android.databinding.generated.callback.OnRefreshListener(this, 1);
        invalidateAll();
    }

    public final void _internalCallbackOnRefresh(int sourceId ) {//刷新监听具体实现
        // localize variables for thread safety
        // vm != null
        boolean vmJavaLangObjectNull = false;
        // vm
        rocketly.mvvmdemo.viewmodel.HotCityListVM vm = mVm;



        vmJavaLangObjectNull = (vm) != (null);
        if (vmJavaLangObjectNull) {


            vm.onRefresh();
        }
    }


public final class OnRefreshListener implements android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {//刷新监听包装类
    final Listener mListener;
    final int mSourceId;
    public OnRefreshListener(Listener listener, int sourceId) {
        mListener = listener;
        mSourceId = sourceId;
    }
    @Override
    public void onRefresh() {
        mListener._internalCallbackOnRefresh(mSourceId );
    }
    public interface Listener {
        void _internalCallbackOnRefresh(int sourceId );
    }
}
```

mapBindings方法就相当于findviewbyId把xml的布局找到装在一个数组中返回，然后赋值给成员变量方便我们调用，并且创建了一个刷新监听，最后调用了一个`invalidateAll()`这个方法最后会调用到`executeBindings()`

```java
    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        rocketly.mvvmdemo.viewmodel.HotCityListVM vm = mVm;
        android.databinding.ObservableArrayList<rocketly.mvvmdemo.model.Basic> vmCityList = null;

        if ((dirtyFlags & 0x7L) != 0) {



                if (vm != null) {
                    // read vm.cityList
                    vmCityList = vm.getCityList();
                }
                updateRegistration(0, vmCityList);
        }
        // batch finished
        if ((dirtyFlags & 0x7L) != 0) {
            // api target 1

            rocketly.mvvmdemo.utils.databinding.DataBindingExKt.setItem(this.rv, vmCityList);
        }
        if ((dirtyFlags & 0x4L) != 0) {
            // api target 1

            this.srl.setOnRefreshListener(mCallback2);
        }
    }
```

可以发现就相当于对srl设置了刷新监听。

所以总结下第二步就是把view传递给binding类然后构造方法findviewbyid找到view赋值给成员变量并把view相关的点击事件刷新等监听初始化好，完成对view的绑定。

#### 第三步传递xml中声明的值给binding，完成对数据的绑定。

将vm传递到binding类中

```java
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            vm = ViewModelProviders.of(this@MainActivity).get(HotCityListVM::class.java)
        }
    }
```

完成对数据的绑定

```java
    public void setVm(@Nullable rocketly.mvvmdemo.viewmodel.HotCityListVM Vm) {
        this.mVm = Vm;
        synchronized(this) {
            mDirtyFlags |= 0x2L;
        }
        notifyPropertyChanged(BR.vm);
        super.requestRebind();
    }
```

`super.requestRebind()`最后也是调到

```java
    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        rocketly.mvvmdemo.viewmodel.HotCityListVM vm = mVm;
        android.databinding.ObservableArrayList<rocketly.mvvmdemo.model.Basic> vmCityList = null;

        if ((dirtyFlags & 0x7L) != 0) {



                if (vm != null) {
                    // read vm.cityList
                    vmCityList = vm.getCityList();//拿到cityList
                }
                updateRegistration(0, vmCityList);//添加监听
        }
        // batch finished
        if ((dirtyFlags & 0x7L) != 0) {
            // api target 1

            rocketly.mvvmdemo.utils.databinding.DataBindingExKt.setItem(this.rv, vmCityList);
        }
        if ((dirtyFlags & 0x4L) != 0) {
            // api target 1

            this.srl.setOnRefreshListener(mCallback2);
        }
    }
```

拿到vm的cityList然后通过updateRegistration方法添加监听，当数据改变的时候就会通知到binding刷新rv的item，这样就实现了对数据的绑定。

经过上面的分析可以看出databinding就是帮我们实现了view和数据的双向绑定并且都是通过原生的方法实现的，所以他是非必须的这些代码我们也可以自己完成。



### 总结

从这3个版本MvvM的使用得到最大的感受还是思维上的转变，由之前的事件驱动转到数据驱动，更多的着眼于数据的变化，至于前面使用的那些库都是为了辅助数据驱动这个原则，其次就是解耦逻辑的抽离使代码结构看起来更加清晰。
