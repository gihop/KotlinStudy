# Anko

<br />

# Anko 소개

- Anko는 코틀린 언어의 제작시인 젯브레인에서 직접 제작하여 배포하는 코틀린 라이브러리로, 안드로이드 애플리케이션 개발에 유용한 유틸리티 함수를 제공한다.
- Anko에서는 유틸리티 함수의 성격에 따라 다음과 같이 네 종류의 라이브러리를 제공한다.
  - Anko Commons
  - Anko Layouts
  - Anko SQLite
  - Anko Coroutines

<br />

# Anko Commons

- Ankko Commons는 안드로이드 애플리케이션을 작성할 때 일반적으로 자주 구현하는 기능을 간편하게 추가할 수 있는 유틸리티 함수를 제공한다.
-  Anko Commons를 사용하려면 이를 사용할 모듈의 빌드스크립트에 다음과 같이 의존성을 추가하면 된다.

### gradle

~~~
//build.gradle

android{
	...
}

dependencies{
	//Anko Commons 라이브러리를 추가한다.
	compile "org.jetbrains.anko:anko-commons:0.10.2"
	
	...
}
~~~

- 서포트 라이브러리에 포함된 클래스를 사용하는 경우, 필요에 따라 anko-appcompat-v7-commons 혹은 anko-support-v4-commons를 다음과 같이 빌드스크립트 내 의존성에 추가하면 된다.

### gradle

~~~
//build.gradle

android{
	...
}

dependencies{
	//Anko Commons 라이브러리를 추가한다.
	compile "org.jetbrains.anko:anko-commons:0.10.2"
	
	//appcompat-v7용 Anko Commons 라이브러리를 추가한다.
	compile "org.jetbrains.anko:anko-appcompat-v7-commons:0.10.2"
	
	//support-v4용 Anko Commons 라이브러리를 추가한다.
	compile "org.jetbrains.anko:anko-support-v4-commons:0.10.2"
}
~~~

## 토스트 표시하기

- toast() 및 longToast() 함수를 사용하면 토스트 메시지를 간편하게 표시할 수 있다.
- 토스트를 표시하려면 Context 클래스의 인스턴스가 필요하므로, 이 클래스 혹은 이를 상속하는 클래스(액티비티, 프래그먼트) 내부에서만 사용할 수 있다.

~~~kotlin
//다음 코드와 동일한 역할을 한다.
//Toast.makeText(Context, "Hello, Kotlin!", Toast.LENGTH_SHORT).show()
toast("Hello, Kotlin!")

//Toast.makeText(Context, R.string.hello, Toast.LENGTH_SHORT).show()
toast(R.string.hello)

//Toast.makeText(Context, "Hello, Kotlin!", Toast.LENGTH_LONG).show()
longToast("Hello, Kotlin!")
~~~

## 다이얼로그 생성 및 표시하기

- alert() 함수를 사용하면 AlertDialog를 생성할 수 있으며, 토스트와 마찬가지로 Context 클래스 혹은 이를 상속하는 클래스(액티비티, 프래그먼트) 내부에서만 사용할 수 있다.

~~~kotlin
//다이얼로그의 제목과 본문을 지정한다
alert(title = "Message", message = "Let's learn Kotlin!"){
  //AlertDialog.Builder.setPositiveButton()에 대응한다.
  positiveButton("Yes"){
    //버튼을 클릭했을 때 수행할 동작을 구현한다.
    toast("Yay!")
  }
  
  //AlertDialog.Builder.setNegativeButton()에 대응한다.
  negativeButton("No"){
    longToast("No way...")
  }
}.show()
~~~

- 프레임워크에서 제공하는 다이얼로그가 아닌 서포트 라이브러리에서 제공하는 다이얼로그(android.support.v7.app.AlertDialog)를 생성하려면, anko-appcompat-v7-commons를 의존성에 추가한 후 다음과 같이 Appcompat를 함수 인자에 추가하면 된다.

~~~kotlin
//import 문이 추가된다.
import org.jetbrains.anko.appcompat.v7.Appcompat

//Appcompat를 인자에 추가한다.
alert(Appcompat, title = "Message", message = "Let's learn Kotlin!"){
  ...
}.show()
~~~

- 여러 항목 중 하나를 선택하도록 할 때 사용하는 리스트 다이얼로그는 selector() 함수를 사용하여 생성할 수 있다.

~~~kotlin
//다이얼로그에 표시할 목록을 생성한다.
val cities = listOf("Seoul", "Tokyo", "Mountain View", "Singapore")

//리스트 다이얼로그를 생성하고 표시한다.
selector(title = "Select City", items = cities){ dlg, selection ->
	//항목을 선택했을 때 수행할 동작을 구현한다.
	toast("You selected ${cities[selection]}")
}
~~~

- 작업의 진행 상태를 표시할 때 사용하는 프로그레스 다이얼로그는 progressDialog()와 indeterminateProgressDialog() 함수를 사용하여 생성할 수 있다.
  - progressDialog() 함수는 파일 다운로드 상태와 같이 진행률을 표시해야 하는 다이얼로그를 생성할 때 사용하며, indeterminateProgressDialog() 함수는 진행률을 표시하지 않는 다이얼로그를 생성할 때 사용한다.

~~~kotlin
//진행률을 표시하는 다이얼로그를 생성한다.
val pd = progressDialog(title = "File Download", message = "Downloading...")

//다이얼로그를 표시한다.
pd.show()

//진행률을 50으로 조정한다.
pd.progress = 50

//진행률을 표시하지 않는 다이얼로그를 생성하고 표시한다.
indeterminateProgressDialog(message = "Please wait...").show()
~~~

## 인텐트 생성 및 사용하기

- 인텐트는 컴포넌트 간에 데이터를 전달할 때에도 사용하지만 주로 액티비티나 서비스를 실행하는 용도로 사용한다. 다른 컴포넌트를 실행하기 위해 인텐트를 사용하는 경우, 이 인텐트는 대상 컴포넌트에 대한 정보와 기타 부가 정보를 포함한다.

~~~kotlin
//DetailActivity 액티비티를 실행하기 위한 인텐트를 생성하는 예.
//DetailActivity 액티비티를 대상 컴포넌트로 지정하는 인텐트.
val intent = Intent(this, DetailActivity::class.java)

//DetailActivity를 실행한다.
startActivity(intent)
~~~

- 이 인텐트에 부가 정보를 추가하거나 플래그를 설정하는 경우 인텐트를 생성하는 코드는 다음과 같이 길어진다.

~~~kotlin
val intent = Intent(this, DetailActivity::class.java)

//인텐트에 부가정보를 추가한다.
intent.putExtra("id", 150L)
intent.putExtra("title", "Awesome item")

//인텐트에 플래그를 설정한다.
intent.setFlag(Intent.FLAG_ACTIVITY_NO_HISTORY)
~~~

- 하지만 intentFor() 함수를 사용하면 훨씬 간소한 형태로 동일한 역할을 하는 인텐트를 생성할 수 있다.

~~~kotlin
val intent = intentFor<DetailActivity>(
	//부가 정보를 Pair 형태로 추가한다.
  "id" to 150L, "title" to "Awosome item")
	//인텐트 플래그를 설정한다.
	.noHistory()
~~~

- 인텐트에 플래그를 지정하지 않는다면, startActivity() 함수나 startService() 함수를 사용하여 인텐트 생성과 컴포넌트 호출을 동시에 수행할 수 있다.
- 이들 함수는 모두 Context 클래스를 필요로 하므로, 이 클래스 혹은 이를 상속하는 클래스(액티비티, 프래그먼트) 내부에서만 사용할 수 있다.

~~~kotlin
//부가정보 없이 DetailActivity를 실행한다.
startActivity<DetailActivity>()

//부가정보를 포함하여 DetailActivity를 실행한다.
startActivity<DetailActivity>("id" to 150L, "title" to "Awesome item")

//부가정보 없이 DataSyncService를 실행한다.
startService<DataSyncService>()

//부가정보를 포함하여 DataSyncService를 실행한다.
startService<DataSyncService>("id" to 1000L)
~~~

- 이 외에도, 자주 사용하는 특정 작업을 바로 수행할 수 있는 함수들을 제공한다.

~~~kotlin
//전화를 거는 인텐트를 실행한다.
makeCall(number = "01012345678")

//문자메시지를 발송하는 인텐트를 실행한다.
sendSMS(number = "01012345678", text = "Hello, Kotlin!")

//웹 페이지를 여는 인텐트를 실행한다.
browse(url = "https://google.com")

//이메일을 발송하는 인텐트를 실행한다.
email(email = "gihop95@gmail.com", subject = "Hello, Jiho Park", text = "How are you?")
~~~

## 로그 메시지 기록하기

- 안드로이드 애플리케이션에서 로그 메시지를 기록하려면 android.util.Log 클래스에서 제공하는 메서드를 사용해야 한다. 
  - 하지만 로그를 기록하는 함수를 호출할때마다 매번 태그를 함께 입력해야 하므로 다소 불편한다.
- Anko 라이브러리에서 제공하는 AnkoLogger를 사용하면 훨씬 편리하게 로그 메시지를 기록할 수 있다. AnkoLogger에서는 다음과 같이 android.util.Log 클래스의 로그 기록 메서드에 대응하는 함수를 제공한다.

| android.util.Log | AnkoLogger |
| ---------------- | ---------- |
| v()              | verbose()  |
| d()              | debug()    |
| i()              | info()     |
| w()              | warn()     |
| e()              | error()    |
| wtf()            | wtf()      |

- AnkoLogger를 사용하려면 이를 사용할 클래스에서 AnkoLogger 인터페이스를 구현하면 된다.
  - 출력할 메시지의 타입으로 String만 허용하는 android.util.Log 클래스와 달리 모든 타입을 허용한다.

~~~kotlin
//AnkoLogger 인터페이스를 구현한다.
class MainActivity: AppCompatActivity(), AnkoLogger{
  fun doSomething(){
    //Log.INFO 레벨로 로그 메시지를 기록한다.
    info("doSomething() called")
  }
  
  fun doSomethingWithParameter(number: Int){
    //Log.DEBUG 레벨로 로그 메시지를 기록한다.
    //String 타입이 아닌 인자는 해당 인자의 toString() 함수 반환값을 기록한다.
    debug(number)
  }
  ...
}
~~~

- AnkoLogger에서 제공하는 함수를 사용하여 로그 메시지를 기록하는 경우, 로그 태그로 해당 함수가 호출되는 클래스의 이름을 사용한다. 따라서 앞의 예제에서는 "MainActivity"를 로그 태그로 사용한다.
  - 로그 태그를 바꾸고 싶다면 다음과 같이 loggerTag 프로퍼티를 오버라이드하면 된다.

~~~kotlin
class MainActivity: AppCompatActivity(), AnkoLogger{
  //이 클래스 내에서 출력되는 로그 태그를 "Main"으로 지정한다.
  override val loggerTag: String
  	get() = "Main"
  
  ...
}
~~~

## 단위 변환하기

- 안드로이드 다양한 기기를 지원하기 위해 픽셀(px) 단위 대신 dip(혹은 dp; device independent pixels)나 sp(scale independent pixels)를 사용한다. dp나 sp단위는 각 단말기의 화면 크기나 밀도에 따라 화면에 표시되는 크기를 일정 비율로 조정하므로, 다양한 화면 크기나 밀도를 가진 단말기에 대응하는 UI를 작성할 때 유용하다.
- 하지만 커스텀 뷰 내부와 같이 뷰에 표시되는 요소의 크기를 픽셀 단위로 다루는 경우 dp나 sp단위를 픽셀 단위로 변환하기 위해 복잡한 과정을 거쳐야 한다.

~~~kotlin
class MainActivity: AppCompatActivity(){
  fun doSomething(){
    //100dp를 픽셀 단위로 변환한다.
    val dpInPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
    
    val spInPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, resources.displayMetrics)
  }
  
  ...
}
~~~

- Anko에서 제공하는 dip() 및 sp() 함수를 사용하면 이러한 단위를 매우 간단히 변환할 수 있다.
- 단위를 변환하기 위해 단말기의 화면 정보를 담고 있는 DisplayMetrics 객체가 필요하므로, 이 함수들은 단말기 화면 정보에 접근할 수 있는 클래스인 Context를 상속한 클래스 혹은 커스텀 뷰 클래스 내에서 사용할 수 있다.
  - TypedValue.applyDimension() 메서드는 Float 형 인자만 지원했지만, dip() 및 sp() 함수는 Int 형 인자도 지원한다.

~~~kotlin
//100dp 픽셀 단위로 변환한다.
val dpInPixel = dip(100)

//16sp를 픽셀 단위로 변환한다.
val spInPixel = sp(16)
~~~

- 반대로, 픽셀 단위를 dp나 sp 단위로 변환하는 함수도 제공한다.

~~~kotlin
//300px를 dp 단위로 변환한다.
val pxInDip = px2dip(300)

//80px를 sp 단위로 변환한다.
val pxInSp = px2sp(80)
~~~

## 기타

- 여러 단말기 환경을 지원하는 애플리케이션은, 단말기 환경에 따라 다른 형태의 UI를 보여주도록 구현하는 경우가 많다. 이러한 경우, configuration() 함수를 사용하면 특정 단말기 환경일 때에만 실행할 코드를 간단하게 구현할 수 있다.
- 다음은 configuration() 함수를 통해 지정할 수 있는 단말기 환경 목록이다.

| 매개변수 이름 | 단말기 환경 종류                      |
| ------------- | ------------------------------------- |
| density       | 화면 밀도                             |
| language      | 시스템 언어                           |
| long          | 화면 길이                             |
| nightMode     | 야간 모드 여부                        |
| orientation   | 화면 방향                             |
| rightToLeft   | RTL(Right-toLeft) 레이아웃의 여부     |
| screenSize    | 화면 크기                             |
| smallestWidth | 화면의 가장 작은 변의 길이            |
| uiMode        | UI 모드 (일반, TV, 차량, 시계, VR 등) |

- 이 함수 또한 단말기 환경에 접근해야 하므로 이 정보에 접근할 수 있는 Context 클래스 혹은 이를 상속한 클래스(액티비티, 프래그먼트)에서만 사용할 수 있다.

~~~kotlin
class MainActivity: AppCompatActivity(){
  fun doSomething(){
		configuration(orientation = Orientation.PORTRAIT){
      //단말기가 세로 방향일 때 수행할 코드를 작성한다.
      ...
    }
    
    configuration(orientation = Orientation.LANDSCAPE, language="ko"){
      //단말기가 가로 방향이면서 시스템 언어가 한국어로 설정되어 있을 때 수행할 코드를 작성한다.
      ...
    }
  }
  ...
}
~~~

- 단순히 단말기의 OS 버전에 따라 분기를 수행하는 경우 doFromSdk()와 doIfSdk()를 사용할 수 있다.

~~~kotlin
doFromSdk(Build.VERSION_CODES.O){
  //안드로이드 8.0 이상 기기에서 수행할 코드를 작성한다.
  ...
}

doIfSdk(Build.VERSION_CODES.N){
  //안드로이드 7.0 이상 기기에서만 수행할 코드를 작성한다.
  ...
}
~~~

<br />

# Anko Layouts

- 안드로이드 애플리케이션을 작성할 때, 대부분 XML 레이아웃을 사용하여 화면을 구성한다. 소스 코드(자바 혹은 코틀린)를 사용하여 화면을 구성하는 것도 가능하지만 XML 레이아웃에 비해 복잡하고 까다로워 대다수의 사람들이 선호하지 않는다.
- 하지만 XML로 작성된 레이아웃을 사용하려면 이 파일에 정의된 뷰를 파싱하는 작업을 먼저 수행해야 한다. 때문에 소스 코드를 사용하여 화면을 구성한 경우에 비해 애플리케이션의 성능이 저하되고, 파싱 과정에서 자원이 더 필요한 만큼 배터리도 더 많이 소모한다.
- Anko Layouts는 소스 코드로 화면을 구성할 때 유용하게 사용할 수 있는 여러 함수들을 제공하며, 이를 사용하면 XML 레이아웃을 작성하는 것처럼 편리하게 소스 코드로도 화면을 구성할 수 있다.
  - Anko Layouts을 사용하려면 이를 사용할 모듈의 빌드스크립트에 의존성을 추가하면 되며, 애플리케이션의 minSdkVersion에 따라 사용하는 라이브러리가 달라진다.

| minSdkVersion   | Anko Layouts 라이브러리 |
| --------------- | ----------------------- |
| 15 이상 19 미만 | anko-sdk15              |
| 19 이상 21 미만 | anko-sdk19              |
| 21 이상 23 미만 | anko-sdk21              |
| 23 이상 25 미만 | anko-sdk23              |
| 25 이상         | anko-sdk25              |

### gradle

~~~kotlin
//minSdkVersion이 15인 모듈에 Anko Layouts 라이브러리를 의존성에 추가한 예제.
android{
  defaultConfig{
    //minSdkVersion이 15로 설정되어 있다.
    minSdkVersion 15
    targetSdkVersion 27
    ...
  }
  ...
}

dependencies{
  //minSdkVersion에 맞추어 Anko Layouts 라이브러리를 추가한다.
  compile "org.jetbrains.anko:anko-anko15:0.10.2"
  ...
}
~~~

- 서포트 라이브러리에 포함된 뷰를 사용하는 경우, 사용하는 뷰가 포함된 라이브러리에 대응하는 Anko Layouts 라이브러리를 의존성에 추가하면 된다.

| 서포트 라이브러리 | Anko Layouts 라이브러리 |
| ----------------- | ----------------------- |
| appcompat-v7      | anko-appcompat-v7       |
| cardview-v7       | anko-cardview-v7        |
| desing            | anko-design             |
| gridlayout-v7     | anko-gridlayout-v7      |
| recyclerview-v7   | anko-recyclerview-v7    |
| support-v4        | anko-support-v4         |

### gradle

~~~kotlin
//서포트 라이브러리와 이에 대응하는 Anko Layouts 라이브러리를 의존성으로 추가한 예시.
android{
  ...
}

dependencies{
  //appcompat-v7 서포트 라이브러리 추가
  compile "com.android.support:appcompat-v7:27.0.1"
  
  //appcompat-v7용 Anko Layouts 라이브러리를 추가
  compile "com.jetbrains.anko:anko-appcompat-v7:0.10.2"
}
~~~

### DSL로 화면 구성하기

- Anko Layouts를 사용하면 소스 코드에서 화면을 DSL(Domain Specific Language) 형태로 정의할 수 있다.
- XML 레이아웃으로 정의할 때보다 더 간단하게 화면을 구성할 수 있다.

~~~kotlin
verticalLayout{
  padding = dip(12)
  
  textView("Enterr Login Credentials")
  
  editText{
    hint = "E-mail"
  }
  
  editText{
    hint = "Password"
  }
  
  button("Submit")
}
~~~

- 앞의 코드에서 사용한 verticalLayout(), textView(), editText(), button()은 Anko Layouts에서 제공하는 함수로, 뷰 혹은 다른 뷰를 포함할 수 있는 레이아웃을 생성하는 역할을 한다.
- 다음은 Anko Layouts에서 제공하는 함수 중 자주 사용하는 함수의 목록이다.

| 함수             | 생성하는 뷰                     | 비고                                                         |
| ---------------- | ------------------------------- | ------------------------------------------------------------ |
| button()         | android.widget.Button           |                                                              |
| checkBox()       | android.widget.CheckBox()       |                                                              |
| editText()       | android.widget.EditText()       |                                                              |
| frameLayout()    | android.widget.FrameLayout()    |                                                              |
| imageView()      | android.widget.ImageView()      |                                                              |
| linearLayout()   | android.widget.LinearLayout()   |                                                              |
| radioButton()    | android.widget.RadioButton()    |                                                              |
| relativeLayout() | android.widget.RelativeLayout() |                                                              |
| switch()         | android.widget.Switch()         | 서포트 라이브러리에서 제공하는 뷰는 switchCompat() 사용      |
| verticalLayout() | android.widget.VerticalLayout() | orientation 값으로 LinearLayout.VERTICAL을 갖는 LinearLayout |
| webView()        | android.widget.WebView()        |                                                              |

- XML 레이아웃 파일에 XML로 구성한 레이아웃을 저장하듯이, DSL로 구성한 뷰는 AnkoComponent 클래스를 컨테이너로 사용한다.
- AnkoComponent에는 정의되어 있는 화면을 표시할 대상 컴포넌트의 정보를 포함하며, 다음은 MainActivity 액티비티에 표시할 뷰의 정보를 가지는 AnkoComponent의 코드 예시를 보여준다.

~~~kotlin
class MainActivityUI: AnkoComponent<MainActivity>{
  override fun createView(ui: AnkoContext<MainActivity>) = ui.apply{
    verticalLayout{
      //LinearLayout의 padding을 12dp로 설정한다.
      padding = dip(12)
      
      //TextView를 추가한다.
      textView("Enter Login Credentials")
      
      //EditText를 추가하고, 힌트 문자열을 설정한다.
      editText{
        hint = "E-mail"
      }
      
      editText{
        hint = "Password"
      }
      
      //버튼을 추가한다.
      button("Submit")
    }
  }.view
}
~~~

## 액티비티에서 사용하기

- AnkoComponent.setContentView(Activity) 함수를 사용하면 AnkoComponent 내에 정의된 화면을 액티비티 화면으로 설정할 수 있다.

~~~kotlin
class MainActivity: AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?){
    super.onCreate(savedInstanceState)
    
    //AnkoComponent에 정의된 뷰를 액티비티 화면으로 설정한다.
    MainActivityUI().setContentView(this)
  }
}

//MainActivity에 표시할 화면을 구성한다.
class MainActivityUI: AnkoComponent<MainActivity>{
  override fun createView(ui: AnkoContext<MainActivity>) = ui.apply{
    verticalLayout{
      padding = dip(12)
      
      textView("Enter Login Credentials")
      
      editText{
        hint = "E-mail"
      }
      
      editText{
        hint = "Password"
      }
      
      button("Submit")
    }
  }.view
}
~~~

- 추가로, 액티비티에서는 AnkoComponent 없이 직접 액티비티 내에서 DSL을 사용하여 화면을 구성할 수 있다.
  - 이 방식으로 화면을 구성하는 경우 setContentView()를 호출하지 않아도 된다.

~~~kotlin
class MainActivity: AppCompatActivity(){
  override fun onCreate(savedInstanceState: Bundle?){
    super.onCreate(savedInstanceState)
    
    //setContentView가 없어도 된다.
    verticalLayout{
      padding = dip(12)
      
      textView("Enter Login Credentials")
      
      editText{
        hint = "E-mail"
      }
      
      editText{
        hint = "Password"
      }
      
      button("Submit")
    }
  }
}
~~~

## 프래그먼트에서 사용하기

- 프래그먼트에서 Anko Layouts를 사용하려면 프래그먼트를 위한 AnkoComponent를 만들고, onCreateView()에서 createView()를 직접 호출하여 프래그먼트의 화면으로 사용할 뷰를 반환하면 된다.
  - createView()를 직접 호출하려면  AnkoContext 객체를 직접 만들어 인자로 전달하면 된다.

~~~kotlin
class MianFragment: Fragment(){
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
    //AnkoComponent.createView() 함수를 호출하여 뷰를 반환한다.
    return MainFragmentUI().createView(AnkoContext.create(context, this))
  }
}

//프래그먼트를 위한 AnkoComponent를 만든다.
class MainFragmentUI: AnkoComponent<MainFragment>{
  override fun createView(ui: AnkoContext<MainFragment>) = ui.apply{
    verticalLayout{
      padding = dip(12)
      
      textView("Enter Login Credentials")
      
      editText{
        hint = "E-mail"
      }
      
      editText{
        hint = "Password"
      }
      
      button("Submit")
    }
  }.view
}
~~~

## Anko Support Plugin

- Anko Support Plugin은 Anko와 같이 사용할 수 있는 부가 기능을 제공하는 IDE 플러그인이다.
- 플러그인을 설치하려면 코틀린 IDE 플러그인을 설치하는 과정과 동일하게 진행하면 되며, 플러그인 검색 다이얼로그에서 'Anko Support'를 선택하여 설치하면 된다.
- Anko Support Plugin에서는 AnkoComponent로 작성한 화면이 어떻게 표시되는지 미리 확인할 수 있는 레이아웃 프리뷰 기능을 제공한다.