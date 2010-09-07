/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */

package scalala;
package generic;

import scalala.collection.domain.DomainException;

//
// Binary operations
//

/**
 * Operation that creates That from A and B.
 * 
 * @author dramage
 */
trait BinaryOp[-A, -B, +That] {
  def apply(a : A, b : B) : That;
}

/** Base class for BinaryOp on a pair of arrays. @author dramage */
class ArrayArrayOp[V1,V2,RV](implicit m : ClassManifest[RV], op : BinaryOp[V1,V2,RV])
extends BinaryOp[Array[V1],Array[V2],Array[RV]] {
  override def apply(a : Array[V1], b : Array[V2]) = {
    if (a.length != b.length) {
      throw new DomainException(this.getClass.getSimpleName + ": arrays have different lengths");
    }
    val rv = new Array[RV](a.length);
    var i = 0;
    while (i < rv.length) {
      rv(i) = op(a(i),b(i));
      i += 1;
    }
    rv;
  }
}

/** Base class for Array (op) Scalar. */
class ArrayScalarOp[V1,V2,RV]
(implicit m : ClassManifest[RV], op : BinaryOp[V1,V2,RV], s : Scalar[V2])
extends BinaryOp[Array[V1],V2,Array[RV]] {
  override def apply(a : Array[V1], b : V2) = {
    val rv = new Array[RV](a.length);
    var i = 0;
    while (i < rv.length) {
      rv(i) = op(a(i),b);
      i += 1;
    }
    rv;
  }
}

/** Base class for Scalar (op) Array. */
class ScalarArrayOp[V1,V2,RV]
(implicit m : ClassManifest[RV], op : BinaryOp[V1,V2,RV], s : Scalar[V1])
extends BinaryOp[V1,Array[V2],Array[RV]] {
  override def apply(a : V1, b : Array[V2]) = {
    val rv = new Array[RV](b.length);
    var i = 0;
    while (i < rv.length) {
      rv(i) = op(a,b(i));
      i += 1;
    }
    rv;
  }
}

/** Base class for BinaryOp on a pair of scala maps. @author dramage */
class MapMapOp[K,V1,V2,RV](implicit op : BinaryOp[V1,V2,RV])
extends BinaryOp[Map[K,V1],Map[K,V2],Map[K,RV]] {
  def apply(a : Map[K,V1], b : Map[K,V2]) =
    (a.keySet ++ b.keySet).map(k => (k,op(a(k),b(k)))).toMap;
}

class Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
(implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
extends BinaryOp[(VA1,VA2),(VB1,VB2),(RV1,RV2)] {
  def apply(a : (VA1,VA2), b : (VB1,VB2)) =
    (op1(a._1,b._1), op2(a._2,b._2));
}

class Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
(implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2],
 op3 : BinaryOp[VA3,VB3,RV3])
extends BinaryOp[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)] {
  def apply(a : (VA1,VA2,VA3), b : (VB1,VB2,VB3)) =
    (op1(a._1,b._1), op2(a._2,b._2), op3(a._3,b._3));
}

class Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
(implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2],
 op3 : BinaryOp[VA3,VB3,RV3], op4 : BinaryOp[VA4,VB4,RV4])
extends BinaryOp[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)] {
  def apply(a : (VA1,VA2,VA3,VA4), b : (VB1,VB2,VB3,VB4)) =
    (op1(a._1,b._1), op2(a._2,b._2), op3(a._3,b._3), op4(a._4, b._4));
}

/** Construction delegate for A :+ B. @author dramage */
trait CanAdd[-A,-B,+That] extends BinaryOp[A,B,That];

object CanAdd {
  type Op[A,B,That] = CanAdd[A,B,That]

  //
  // Primitives
  //

  implicit object OpII extends Op[Int,Int,Int]
  { def apply(a : Int, b : Int) = a + b; }

  implicit object OpIL extends Op[Int,Long,Long]
  { def apply(a : Int, b : Long) = a + b; }

  implicit object OpIF extends Op[Int,Float,Float]
  { def apply(a : Int, b : Float) = a + b; }

  implicit object OpID extends Op[Int,Double,Double]
  { def apply(a : Int, b : Double) = a + b; }

  implicit object OpLI extends Op[Long,Int,Long]
  { def apply(a : Long, b : Int) = a + b; }

  implicit object OpLL extends Op[Long,Long,Long]
  { def apply(a : Long, b : Long) = a + b; }

  implicit object OpLF extends Op[Long,Float,Double]
  { def apply(a : Long, b : Float) = a + b; }

  implicit object OpLD extends Op[Long,Double,Double]
  { def apply(a : Long, b : Double) = a + b; }

  implicit object OpFI extends Op[Float,Int,Float]
  { def apply(a : Float, b : Int) = a + b; }

  implicit object OpFL extends Op[Float,Long,Double]
  { def apply(a : Float, b : Long) = a + b; }

  implicit object OpFF extends Op[Float,Float,Float]
  { def apply(a : Float, b : Float) = a + b; }

  implicit object OpFD extends Op[Float,Double,Double]
  { def apply(a : Float, b : Double) = a + b; }

  implicit object OpDI extends Op[Double,Int,Double]
  { def apply(a : Double, b : Int) = a + b; }

  implicit object OpDL extends Op[Double,Long,Double]
  { def apply(a : Double, b : Long) = a + b; }

  implicit object OpDF extends Op[Double,Float,Double]
  { def apply(a : Double, b : Float) = a + b; }

  implicit object OpDD extends Op[Double,Double,Double]
  { def apply(a : Double, b : Double) = a + b; }

  //
  //
  // Below this line is copy-and-paste between ops' companion objects
  //
  //

  //
  // Arrays
  //

  implicit def opArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  = new OpArrayArray[V1,V2,RV];

  class OpArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  extends ArrayArrayOp[V1,V2,RV] with Op[Array[V1],Array[V2],Array[RV]];

  implicit object OpArrayArrayII extends OpArrayArray[Int,Int,Int];
  implicit object OpArrayArrayDD extends OpArrayArray[Double,Double,Double];
  implicit object OpArrayArrayDI extends OpArrayArray[Double,Int,Double];
  implicit object OpArrayArrayID extends OpArrayArray[Int,Double,Double];

  implicit def opArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  = new OpArrayScalar[V1,V2,RV];

  class OpArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  extends ArrayScalarOp[V1,V2,RV] with Op[Array[V1],V2,Array[RV]];

  implicit object OpArrayScalarII extends OpArrayScalar[Int,Int,Int];
  implicit object OpArrayScalarDD extends OpArrayScalar[Double,Double,Double];
  implicit object OpArrayScalarDI extends OpArrayScalar[Double,Int,Double];
  implicit object OpArrayScalarID extends OpArrayScalar[Int,Double,Double];

  implicit def opScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  = new OpScalarArray[V1,V2,RV];

  class OpScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  extends ScalarArrayOp[V1,V2,RV] with Op[V1,Array[V2],Array[RV]];

  implicit object OpScalarArrayII extends OpScalarArray[Int,Int,Int];
  implicit object OpScalarArrayDD extends OpScalarArray[Double,Double,Double];
  implicit object OpScalarArrayDI extends OpScalarArray[Double,Int,Double];
  implicit object OpScalarArrayID extends OpScalarArray[Int,Double,Double];

  //
  // Tuples
  //

  implicit def opTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2]) =
    new OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2];

  class OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
  extends Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
     with Op[(VA1,VA2),(VB1,VB2),(RV1,RV2)];

  implicit def opTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3]) =
    new OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3];

  class OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3])
  extends Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
     with Op[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)];

  implicit def opTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4]) =
    new OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4];

  class OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4])
  extends Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
     with Op[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)];


  //
  // Scala Maps
  //

  implicit def opMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV]) =
    new OpMap[K,V1,V2,RV];

  class OpMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV])
  extends MapMapOp[K,V1,V2,RV] with Op[Map[K,V1],Map[K,V2],Map[K,RV]];
}

/** Construction delegate for A :- B. @author dramage */
trait CanSub[-A,-B,+That] extends BinaryOp[A,B,That];

object CanSub {
 type Op[A,B,That] = CanSub[A,B,That]

  //
  // Primitives
  //

  implicit object OpII extends Op[Int,Int,Int]
  { def apply(a : Int, b : Int) = a - b; }

  implicit object OpIL extends Op[Int,Long,Long]
  { def apply(a : Int, b : Long) = a - b; }

  implicit object OpIF extends Op[Int,Float,Float]
  { def apply(a : Int, b : Float) = a - b; }

  implicit object OpID extends Op[Int,Double,Double]
  { def apply(a : Int, b : Double) = a - b; }

  implicit object OpLI extends Op[Long,Int,Long]
  { def apply(a : Long, b : Int) = a - b; }

  implicit object OpLL extends Op[Long,Long,Long]
  { def apply(a : Long, b : Long) = a - b; }

  implicit object OpLF extends Op[Long,Float,Double]
  { def apply(a : Long, b : Float) = a - b; }

  implicit object OpLD extends Op[Long,Double,Double]
  { def apply(a : Long, b : Double) = a - b; }

  implicit object OpFI extends Op[Float,Int,Float]
  { def apply(a : Float, b : Int) = a - b; }

  implicit object OpFL extends Op[Float,Long,Double]
  { def apply(a : Float, b : Long) = a - b; }

  implicit object OpFF extends Op[Float,Float,Float]
  { def apply(a : Float, b : Float) = a - b; }

  implicit object OpFD extends Op[Float,Double,Double]
  { def apply(a : Float, b : Double) = a - b; }

  implicit object OpDI extends Op[Double,Int,Double]
  { def apply(a : Double, b : Int) = a - b; }

  implicit object OpDL extends Op[Double,Long,Double]
  { def apply(a : Double, b : Long) = a - b; }

  implicit object OpDF extends Op[Double,Float,Double]
  { def apply(a : Double, b : Float) = a - b; }

  implicit object OpDD extends Op[Double,Double,Double]
  { def apply(a : Double, b : Double) = a - b; }

  //
  //
  // Below this line is copy-and-paste between ops' companion objects
  //
  //

  //
  // Arrays
  //

  implicit def opArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  = new OpArrayArray[V1,V2,RV];

  class OpArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  extends ArrayArrayOp[V1,V2,RV] with Op[Array[V1],Array[V2],Array[RV]];

  implicit object OpArrayArrayII extends OpArrayArray[Int,Int,Int];
  implicit object OpArrayArrayDD extends OpArrayArray[Double,Double,Double];
  implicit object OpArrayArrayDI extends OpArrayArray[Double,Int,Double];
  implicit object OpArrayArrayID extends OpArrayArray[Int,Double,Double];

  implicit def opArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  = new OpArrayScalar[V1,V2,RV];

  class OpArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  extends ArrayScalarOp[V1,V2,RV] with Op[Array[V1],V2,Array[RV]];

  implicit object OpArrayScalarII extends OpArrayScalar[Int,Int,Int];
  implicit object OpArrayScalarDD extends OpArrayScalar[Double,Double,Double];
  implicit object OpArrayScalarDI extends OpArrayScalar[Double,Int,Double];
  implicit object OpArrayScalarID extends OpArrayScalar[Int,Double,Double];

  implicit def opScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  = new OpScalarArray[V1,V2,RV];

  class OpScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  extends ScalarArrayOp[V1,V2,RV] with Op[V1,Array[V2],Array[RV]];

  implicit object OpScalarArrayII extends OpScalarArray[Int,Int,Int];
  implicit object OpScalarArrayDD extends OpScalarArray[Double,Double,Double];
  implicit object OpScalarArrayDI extends OpScalarArray[Double,Int,Double];
  implicit object OpScalarArrayID extends OpScalarArray[Int,Double,Double];

  //
  // Tuples
  //

  implicit def opTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2]) =
    new OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2];

  class OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
  extends Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
     with Op[(VA1,VA2),(VB1,VB2),(RV1,RV2)];

  implicit def opTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3]) =
    new OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3];

  class OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3])
  extends Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
     with Op[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)];

  implicit def opTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4]) =
    new OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4];

  class OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4])
  extends Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
     with Op[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)];


  //
  // Scala Maps
  //

  implicit def opMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV]) =
    new OpMap[K,V1,V2,RV];

  class OpMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV])
  extends MapMapOp[K,V1,V2,RV] with Op[Map[K,V1],Map[K,V2],Map[K,RV]];
}

/** Construction delegate for A :* B. @author dramage */
trait CanMul[-A,-B,+That] extends BinaryOp[A,B,That];

object CanMul {
 type Op[A,B,That] = CanMul[A,B,That]

  //
  // Primitives
  //

  implicit object OpII extends Op[Int,Int,Int]
  { def apply(a : Int, b : Int) = a * b; }

  implicit object OpIL extends Op[Int,Long,Long]
  { def apply(a : Int, b : Long) = a * b; }

  implicit object OpIF extends Op[Int,Float,Float]
  { def apply(a : Int, b : Float) = a * b; }

  implicit object OpID extends Op[Int,Double,Double]
  { def apply(a : Int, b : Double) = a * b; }

  implicit object OpLI extends Op[Long,Int,Long]
  { def apply(a : Long, b : Int) = a * b; }

  implicit object OpLL extends Op[Long,Long,Long]
  { def apply(a : Long, b : Long) = a * b; }

  implicit object OpLF extends Op[Long,Float,Double]
  { def apply(a : Long, b : Float) = a * b; }

  implicit object OpLD extends Op[Long,Double,Double]
  { def apply(a : Long, b : Double) = a * b; }

  implicit object OpFI extends Op[Float,Int,Float]
  { def apply(a : Float, b : Int) = a * b; }

  implicit object OpFL extends Op[Float,Long,Double]
  { def apply(a : Float, b : Long) = a * b; }

  implicit object OpFF extends Op[Float,Float,Float]
  { def apply(a : Float, b : Float) = a * b; }

  implicit object OpFD extends Op[Float,Double,Double]
  { def apply(a : Float, b : Double) = a * b; }

  implicit object OpDI extends Op[Double,Int,Double]
  { def apply(a : Double, b : Int) = a * b; }

  implicit object OpDL extends Op[Double,Long,Double]
  { def apply(a : Double, b : Long) = a * b; }

  implicit object OpDF extends Op[Double,Float,Double]
  { def apply(a : Double, b : Float) = a * b; }

  implicit object OpDD extends Op[Double,Double,Double]
  { def apply(a : Double, b : Double) = a * b; }

  //
  //
  // Below this line is copy-and-paste between ops' companion objects
  //
  //

  //
  // Arrays
  //

  implicit def opArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  = new OpArrayArray[V1,V2,RV];

  class OpArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  extends ArrayArrayOp[V1,V2,RV] with Op[Array[V1],Array[V2],Array[RV]];

  implicit object OpArrayArrayII extends OpArrayArray[Int,Int,Int];
  implicit object OpArrayArrayDD extends OpArrayArray[Double,Double,Double];
  implicit object OpArrayArrayDI extends OpArrayArray[Double,Int,Double];
  implicit object OpArrayArrayID extends OpArrayArray[Int,Double,Double];

  implicit def opArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  = new OpArrayScalar[V1,V2,RV];

  class OpArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  extends ArrayScalarOp[V1,V2,RV] with Op[Array[V1],V2,Array[RV]];

  implicit object OpArrayScalarII extends OpArrayScalar[Int,Int,Int];
  implicit object OpArrayScalarDD extends OpArrayScalar[Double,Double,Double];
  implicit object OpArrayScalarDI extends OpArrayScalar[Double,Int,Double];
  implicit object OpArrayScalarID extends OpArrayScalar[Int,Double,Double];

  implicit def opScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  = new OpScalarArray[V1,V2,RV];

  class OpScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  extends ScalarArrayOp[V1,V2,RV] with Op[V1,Array[V2],Array[RV]];

  implicit object OpScalarArrayII extends OpScalarArray[Int,Int,Int];
  implicit object OpScalarArrayDD extends OpScalarArray[Double,Double,Double];
  implicit object OpScalarArrayDI extends OpScalarArray[Double,Int,Double];
  implicit object OpScalarArrayID extends OpScalarArray[Int,Double,Double];

  //
  // Tuples
  //

  implicit def opTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2]) =
    new OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2];

  class OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
  extends Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
     with Op[(VA1,VA2),(VB1,VB2),(RV1,RV2)];

  implicit def opTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3]) =
    new OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3];

  class OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3])
  extends Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
     with Op[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)];

  implicit def opTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4]) =
    new OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4];

  class OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4])
  extends Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
     with Op[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)];


  //
  // Scala Maps
  //

  implicit def opMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV]) =
    new OpMap[K,V1,V2,RV];

  class OpMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV])
  extends MapMapOp[K,V1,V2,RV] with Op[Map[K,V1],Map[K,V2],Map[K,RV]];
}


/** Construction delegate for A :/ B. @author dramage */
trait CanDiv[-A,-B,+That] extends BinaryOp[A,B,That];

object CanDiv {
 type Op[A,B,That] = CanDiv[A,B,That]

  //
  // Primitives
  //

  implicit object OpII extends Op[Int,Int,Int]
  { def apply(a : Int, b : Int) = a / b; }

  implicit object OpIL extends Op[Int,Long,Long]
  { def apply(a : Int, b : Long) = a / b; }

  implicit object OpIF extends Op[Int,Float,Float]
  { def apply(a : Int, b : Float) = a / b; }

  implicit object OpID extends Op[Int,Double,Double]
  { def apply(a : Int, b : Double) = a / b; }

  implicit object OpLI extends Op[Long,Int,Long]
  { def apply(a : Long, b : Int) = a / b; }

  implicit object OpLL extends Op[Long,Long,Long]
  { def apply(a : Long, b : Long) = a / b; }

  implicit object OpLF extends Op[Long,Float,Double]
  { def apply(a : Long, b : Float) = a / b; }

  implicit object OpLD extends Op[Long,Double,Double]
  { def apply(a : Long, b : Double) = a / b; }

  implicit object OpFI extends Op[Float,Int,Float]
  { def apply(a : Float, b : Int) = a / b; }

  implicit object OpFL extends Op[Float,Long,Double]
  { def apply(a : Float, b : Long) = a / b; }

  implicit object OpFF extends Op[Float,Float,Float]
  { def apply(a : Float, b : Float) = a / b; }

  implicit object OpFD extends Op[Float,Double,Double]
  { def apply(a : Float, b : Double) = a / b; }

  implicit object OpDI extends Op[Double,Int,Double]
  { def apply(a : Double, b : Int) = a / b; }

  implicit object OpDL extends Op[Double,Long,Double]
  { def apply(a : Double, b : Long) = a / b; }

  implicit object OpDF extends Op[Double,Float,Double]
  { def apply(a : Double, b : Float) = a / b; }

  implicit object OpDD extends Op[Double,Double,Double]
  { def apply(a : Double, b : Double) = a / b; }

  //
  //
  // Below this line is copy-and-paste between ops' companion objects
  //
  //

  //
  // Arrays
  //

  implicit def opArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  = new OpArrayArray[V1,V2,RV];

  class OpArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  extends ArrayArrayOp[V1,V2,RV] with Op[Array[V1],Array[V2],Array[RV]];

  implicit object OpArrayArrayII extends OpArrayArray[Int,Int,Int];
  implicit object OpArrayArrayDD extends OpArrayArray[Double,Double,Double];
  implicit object OpArrayArrayDI extends OpArrayArray[Double,Int,Double];
  implicit object OpArrayArrayID extends OpArrayArray[Int,Double,Double];

  implicit def opArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  = new OpArrayScalar[V1,V2,RV];

  class OpArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  extends ArrayScalarOp[V1,V2,RV] with Op[Array[V1],V2,Array[RV]];

  implicit object OpArrayScalarII extends OpArrayScalar[Int,Int,Int];
  implicit object OpArrayScalarDD extends OpArrayScalar[Double,Double,Double];
  implicit object OpArrayScalarDI extends OpArrayScalar[Double,Int,Double];
  implicit object OpArrayScalarID extends OpArrayScalar[Int,Double,Double];

  implicit def opScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  = new OpScalarArray[V1,V2,RV];

  class OpScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  extends ScalarArrayOp[V1,V2,RV] with Op[V1,Array[V2],Array[RV]];

  implicit object OpScalarArrayII extends OpScalarArray[Int,Int,Int];
  implicit object OpScalarArrayDD extends OpScalarArray[Double,Double,Double];
  implicit object OpScalarArrayDI extends OpScalarArray[Double,Int,Double];
  implicit object OpScalarArrayID extends OpScalarArray[Int,Double,Double];

  //
  // Tuples
  //

  implicit def opTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2]) =
    new OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2];

  class OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
  extends Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
     with Op[(VA1,VA2),(VB1,VB2),(RV1,RV2)];

  implicit def opTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3]) =
    new OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3];

  class OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3])
  extends Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
     with Op[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)];

  implicit def opTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4]) =
    new OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4];

  class OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4])
  extends Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
     with Op[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)];


  //
  // Scala Maps
  //

  implicit def opMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV]) =
    new OpMap[K,V1,V2,RV];

  class OpMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV])
  extends MapMapOp[K,V1,V2,RV] with Op[Map[K,V1],Map[K,V2],Map[K,RV]];
}

/** Construction delegate for A :% B. @author dramage */
trait CanMod[-A,-B,+That] extends BinaryOp[A,B,That];

object CanMod {
 type Op[A,B,That] = CanMod[A,B,That]

  //
  // Primitives
  //

  implicit object OpII extends Op[Int,Int,Int]
  { def apply(a : Int, b : Int) = a % b; }

  implicit object OpIL extends Op[Int,Long,Long]
  { def apply(a : Int, b : Long) = a % b; }

  implicit object OpIF extends Op[Int,Float,Float]
  { def apply(a : Int, b : Float) = a % b; }

  implicit object OpID extends Op[Int,Double,Double]
  { def apply(a : Int, b : Double) = a % b; }

  implicit object OpLI extends Op[Long,Int,Long]
  { def apply(a : Long, b : Int) = a % b; }

  implicit object OpLL extends Op[Long,Long,Long]
  { def apply(a : Long, b : Long) = a % b; }

  implicit object OpLF extends Op[Long,Float,Double]
  { def apply(a : Long, b : Float) = a % b; }

  implicit object OpLD extends Op[Long,Double,Double]
  { def apply(a : Long, b : Double) = a % b; }

  implicit object OpFI extends Op[Float,Int,Float]
  { def apply(a : Float, b : Int) = a % b; }

  implicit object OpFL extends Op[Float,Long,Double]
  { def apply(a : Float, b : Long) = a % b; }

  implicit object OpFF extends Op[Float,Float,Float]
  { def apply(a : Float, b : Float) = a % b; }

  implicit object OpFD extends Op[Float,Double,Double]
  { def apply(a : Float, b : Double) = a % b; }

  implicit object OpDI extends Op[Double,Int,Double]
  { def apply(a : Double, b : Int) = a % b; }

  implicit object OpDL extends Op[Double,Long,Double]
  { def apply(a : Double, b : Long) = a % b; }

  implicit object OpDF extends Op[Double,Float,Double]
  { def apply(a : Double, b : Float) = a % b; }

  implicit object OpDD extends Op[Double,Double,Double]
  { def apply(a : Double, b : Double) = a % b; }

  //
  //
  // Below this line is copy-and-paste between ops' companion objects
  //
  //

  //
  // Arrays
  //

  implicit def opArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  = new OpArrayArray[V1,V2,RV];

  class OpArrayArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV])
  extends ArrayArrayOp[V1,V2,RV] with Op[Array[V1],Array[V2],Array[RV]];

  implicit object OpArrayArrayII extends OpArrayArray[Int,Int,Int];
  implicit object OpArrayArrayDD extends OpArrayArray[Double,Double,Double];
  implicit object OpArrayArrayDI extends OpArrayArray[Double,Int,Double];
  implicit object OpArrayArrayID extends OpArrayArray[Int,Double,Double];

  implicit def opArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  = new OpArrayScalar[V1,V2,RV];

  class OpArrayScalar[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V2])
  extends ArrayScalarOp[V1,V2,RV] with Op[Array[V1],V2,Array[RV]];

  implicit object OpArrayScalarII extends OpArrayScalar[Int,Int,Int];
  implicit object OpArrayScalarDD extends OpArrayScalar[Double,Double,Double];
  implicit object OpArrayScalarDI extends OpArrayScalar[Double,Int,Double];
  implicit object OpArrayScalarID extends OpArrayScalar[Int,Double,Double];

  implicit def opScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  = new OpScalarArray[V1,V2,RV];

  class OpScalarArray[V1,V2,RV](implicit m : ClassManifest[RV], op : Op[V1,V2,RV], s : Scalar[V1])
  extends ScalarArrayOp[V1,V2,RV] with Op[V1,Array[V2],Array[RV]];

  implicit object OpScalarArrayII extends OpScalarArray[Int,Int,Int];
  implicit object OpScalarArrayDD extends OpScalarArray[Double,Double,Double];
  implicit object OpScalarArrayDI extends OpScalarArray[Double,Int,Double];
  implicit object OpScalarArrayID extends OpScalarArray[Int,Double,Double];

  //
  // Tuples
  //

  implicit def opTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2]) =
    new OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2];

  class OpTuple2Tuple2[VA1,VA2,VB1,VB2,RV1,RV2]
  (implicit op1 : BinaryOp[VA1,VB1,RV1], op2 : BinaryOp[VA2,VB2,RV2])
  extends Tuple2Tuple2Op[VA1,VA2,VB1,VB2,RV1,RV2]
     with Op[(VA1,VA2),(VB1,VB2),(RV1,RV2)];

  implicit def opTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3]) =
    new OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3];

  class OpTuple3Tuple3[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3])
  extends Tuple3Tuple3Op[VA1,VA2,VA3,VB1,VB2,VB3,RV1,RV2,RV3]
     with Op[(VA1,VA2,VA3),(VB1,VB2,VB3),(RV1,RV2,RV3)];

  implicit def opTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4]) =
    new OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4];

  class OpTuple4Tuple4[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
  (implicit op1 : Op[VA1,VB1,RV1], op2 : Op[VA2,VB2,RV2], op3 : Op[VA3,VB3,RV3], op4 : Op[VA4,VB4,RV4])
  extends Tuple4Tuple4Op[VA1,VA2,VA3,VA4,VB1,VB2,VB3,VB4,RV1,RV2,RV3,RV4]
     with Op[(VA1,VA2,VA3,VA4),(VB1,VB2,VB3,VB4),(RV1,RV2,RV3,RV4)];


  //
  // Scala Maps
  //

  implicit def opMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV]) =
    new OpMap[K,V1,V2,RV];

  class OpMap[K,V1,V2,RV](implicit op : Op[V1,V2,RV])
  extends MapMapOp[K,V1,V2,RV] with Op[Map[K,V1],Map[K,V2],Map[K,RV]];
}

/** Construction delegate for A :^ B. @author dramage */
trait CanPow[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :< B. @author dramage */
trait CanLT[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :<= B. @author dramage */
trait CanLTE[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :> B. @author dramage */
trait CanGT[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :>= B. @author dramage */
trait CanGTE[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :== B. @author dramage */
trait CanEq[-A,-B,+That] extends BinaryOp[A,B,That];

/** Construction delegate for A :!= B. @author dramage */
trait CanNe[-A,-B,+That] extends BinaryOp[A,B,That];
