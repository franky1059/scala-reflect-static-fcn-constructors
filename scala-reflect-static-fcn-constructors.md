

Reflective Instantiation of Classes at Runtime 
--------------------------------------
#### Dynamic and Reflective Variable and Method Processing
Roughly stated, Reflection is the ability for a program to inspect properties of itself and dynamically identify and invoke particular variables and methods. The needs for this type of mechanism vary, for example maybe you have an etl processing script that needs to dynamically instantiate a class based on meta data store in a data-source. Another scenario is you want to build a customized performance monitoring routine that looks at what kind of objects are running and their influence on the system's memory resources. 

#### Trade-offs with Run-time Objects
The biggest trade-off when using reflective operations in a compiled language like Scala is that you run the risk of not finding code issues during the compilation process, thus nullifying one of the major proponents of using a compiled language for your application. <br/> 
The major proponent of reflective instantiation is de-coupling. Since the mechanism that locates and runs a class or function always uses the same input you don't need to build and maintain large if-else statements every-time a new instantiation code-block is created. This is important because it's one less portion of code to remember to maintain or change when developing the code-base - which means one less point-of-failure. This is critical when things like developer turn-over, rapid hot-fixes, security audits and other common software work-flow activities occur. <br/>
It also makes design patterns like plug-ins, observers, and post-release add-ons possible for software products or managed services. You can also silo code-base access and permissions this way if you want certain personnel to only have visibility to isolated components of the overall code-base. 

#### Reflection in Scala
As with most compiled languages, reflection with Scala is a totally complex and confusing journey to understand and implement. Scala provides Runtime and Compile-time reflection approaches. We'll focus on the Runtime approach, which uses programmatic methods to inspect objects during the execution of a program. The compile-time approach relies on macros that are fed to the compiler to "auto-create" all the code necessary to inspect objects at runtime. There's nothing wrong with this method, but in my opinion it makes your project less portable - mainly bcs you need to remember to provide your macros along with the code you compile. <br/>
Scala uses various elements in its framework to implement reflection, like Environments, Symbols, Mirrors, Types, etc.  As said before, it's all really confusing and difficult to gain a complete understanding purely from Scala's documentation. The best advice I used was to rely on the REPL to experiment and inspect the results of the reflection api so you can understand what each method produces. 


#### Reflecting Constructors
Our goal is to dynamically instantiate an object using nothing but a string value which is provided during the runtime execution of our program. This translates to Scala's reflective framework as: Inspecting the target class's properties that are of method type, Filtering the method types by the constructor type, Creating a mirror for the constructor, Then invoking the constructor mirror. Here's an example of this done in Scala v2.10
<pre>
val m = ru.runtimeMirror(getClass.getClassLoader)
val clz = Class.forName(class_name)
val classSymbol = m.classSymbol(clz)
val c = m.reflectClass(classSymbol)
val c_Type = classSymbol.toType
val c_ctor = c_Type.declaration(ru.nme.CONSTRUCTOR).asMethod
val c_ctorm = c.reflectConstructor(c_ctor)
val instantiate_object = c_ctorm()
</pre>


#### Classes with Multiple Constructors 
That's all well and good, but let's introduce a caveat which is to instantiate a class which has overloaded constructors. The process is similar with the added tasks of inspecting the alternatives property symbols of the constructor type for the class, Then matching the each symbol to the desired constructor argument definition that we want to use to instantiate our object. 
<pre>
val m = ru.runtimeMirror(getClass.getClassLoader)
val clz = Class.forName(class_name)
val classSymbol = m.classSymbol(clz)
val c = m.reflectClass(classSymbol)
val c_Type = classSymbol.toType		
val alternatives = c_Type.declaration(nme.CONSTRUCTOR).asTerm.alternatives.collect {
			case m: MethodSymbol if m.isConstructor => { m }
	}
val mkCnstrTup = (m: MethodSymbol) => {
	val flat_paramss = m.paramss.map(_.map(_.name.toString())).flatten 
	Tuple2(m , flat_paramss)
}
var mapped_alts = alternatives.map(mkCnstrTup(_))
val ctorTupHasArg = (ctor_tup: Tuple2[MethodSymbol, List[String]], arg_name: List[String]) => {
	arg_name forall (ctor_tup._2 contains)
}
val ctro_indx = mapped_alts find (ctorTupHasArg(_, ctor_params))
val c_ctor = ctro_indx.get._1.asMethod
val c_ctorm = c.reflectConstructor(c_ctor)
</pre>


#### Conclusion
You'll notice a similar theme for every reflective operation and that is you'll be searching and converting properties of classes to types, symbols, and mirrors. This will become maddeningly frustrating as you try to achieve your goal because you'll often have the exact property you want to use but have it in its wrong structural element within Scala's programmatic framework. As mentioned before, your best friend will be the REPL which you can use to quickly trial and error the results of various reflective commands. The documentation will give you 70% of the knowledge you need - almost as if they know exactly what you want to do, but decide to toy with you by omitting the one piece of information you need to fill in the missing pieces. 




Code
--------------------------------------
- [scala-reflect-static-fcn-constructors (GitHub)](https://github.com/franky1059/scala-reflect-static-fcn-constructors)



Links
--------------------------------------
- [Reflection Overview (Scala Docs)](http://docs.scala-lang.org/overviews/reflection/overview.html)
- [Symbols, Trees, and Types (Scala Docs)](http://docs.scala-lang.org/overviews/reflection/symbols-trees-types.html)
- [Environment, Universes, and Mirrors (Scala Docs)](http://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html)
- [How to get constructor argument names using Scala-Macros](http://stackoverflow.com/questions/13814288/how-to-get-constructor-argument-names-using-scala-macros)
- [Unable to add scala-reflect as a dependency (stack overflow)](https://stackoverflow.com/questions/22226874/unable-to-add-scala-reflect-as-a-dependency)




