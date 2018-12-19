// ../sbt/bin/sbt package
// ../sbt/bin/sbt "runMain ReflectConstructors <fcn to run>"


import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._

import scala.collection.mutable.ListBuffer



class ClassWthConstructors {

	var prop_var: Int = 0


	def this(args: Map[String, Any]) {
		this
	}

}




object ReflectConstructors {

	def object_constructor_resolve(class_name: String): MethodMirror = {
		val m = ru.runtimeMirror(getClass.getClassLoader)
		val clz = Class.forName(class_name)
		val classSymbol = m.classSymbol(clz)
		val c = m.reflectClass(classSymbol)
		val c_Type = classSymbol.toType
		val c_ctor = c_Type.declaration(ru.nme.CONSTRUCTOR).asMethod
		val c_ctorm = c.reflectConstructor(c_ctor)
		return c_ctorm
    }


    def object_constructor_w_ctor_params_resolve(class_name: String, ctor_params: List[String]): MethodMirror = {
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

		return c_ctorm
    }	





	def main(args: Array[String]) {

		val class_constructor = object_constructor_w_ctor_params_resolve("ClassWthConstructors", List("args"))
		val handler_obj = class_constructor(Map("key" -> "val"))
		println(handler_obj)
	}


}