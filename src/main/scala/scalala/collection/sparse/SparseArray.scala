package scalala.collection.sparse;

/*
 Copyright 2009 David Hall, Daniel Ramage
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at 
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
*/

import scala.collection.generic._;
import scala.reflect.ClassManifest;
import scala.collection.mutable._;

/**
 * Treats an array as a sparse array.  Logarithmic access time.
 *
 * @param length The virtual length of the array.
 * @param default The default value of elements not in the sparse array.
 * @param initial The initial length of the sparse data structures.
 *
 * @author dlwh, dramage
 */
class SparseArray[@specialized T:ClassManifest]
(val length : Int, initialSize : Int = 3)(val default : T) {

  /** Data array.  Only the first this.used elements are valid. */
  protected var data = new Array[T](initialSize);

  /** Index array.  Only the first this.used elements are valid. */
  protected var index = new Array[Int](initialSize);

  /** Number of elements in the array that have been used. */
  protected var used : Int = 0;

  /** Last found offset. */
  private var lastOffset = -1;

  /** Index at last found offset. */
  private var lastIndex = -1;

  /** Returns the size of the array. */
  def size = used;

  /** Applies the given function to each non-default element. */
  def foreach[U](f: Function1[(Int,T),U]) {
    var i = 0;
    while(i < used) {
      f(index(i), data(i));
      i += 1;
    }
  }

  /** Iterator over used indexes and values */
  final def iterator = Iterator.range(0, used).map(i => (index(i),data(i)));

  /** Used indexes. */
  def keysIterator = index.iterator.take(used);

  /** Used values. */
  def valuesIterator = data.iterator.take(used);

  /** A copy of the keys in this array. */
  def keysArray : Array[Int] = index.take(used);

  /** A copy of the values in this array. */
  def valuesArray : Array[T] = data.take(used);

  // Taken from Scalala

  /** Records that the given index was found at this.index(offset). */
  final private def found(index : Int, offset : Int) : Int = {
    lastOffset = offset;
    lastIndex = index;
    return offset;
  }

  /**
   * Returns the offset into index and data for the requested vector
   * index.  If the requested index is not found, the return value is
   * negative and can be converted into an insertion point with ~rv.
   */
  private def findOffset(i : Int) : Int = {
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException("Index "+i+" out of bounds [0,"+size+")");

    if (i == lastIndex) {
      // previous element; don't need to update lastOffset
      return lastOffset;
    } else if (used == 0) {
      // empty list; do nothing
      return -1;
    } else {
      // regular binary search
      var begin = 0;
      var end = used - 1;

      // narrow the search if we have a previous reference
      if (lastIndex >= 0 && lastOffset >= 0) {
        if (i < lastIndex) {
          // in range preceding last request
          end = lastOffset;
        } else {
          // in range following last request
          begin = lastOffset;

          if (begin + 1 <= end && index(begin + 1) == i) {
            // special case: successor of last request
            return found(i, begin + 1);
          }
        }
      }

      // Simple optimization:
      // the i'th entry can't be after entry i.
      if(end > i)
        end = i;

      var mid = (end + begin) >> 1;
      while (begin <= end) {
        mid = (end + begin) >> 1;
        if (index(mid) < i)
          begin = mid + 1;
        else if (index(mid) > i)
          end = mid - 1;
        else
          return found(i, mid);
      }

      // no match found, return insertion point
      if (i <= index(mid))
        return ~mid;       // Insert here (before mid)
      else
        return ~(mid + 1); // Insert after mid
    }
  }

  def apply(i : Int) : T = {
    val offset = findOffset(i);
    if (offset >= 0) data(offset) else default;
  }

  def get(i: Int) : Option[T] = {
    val offset = findOffset(i);
    if (offset >= 0) Some(data(offset)) else None;
  }

  def getOrElse(i : Int, value : T) : T = {
    val offset = findOffset(i);
    if (offset >= 0) data(offset) else value;
  }

  /**
   * Sets the given value at the given index if the value is not
   * equal to the current default.  The data and
   * index arrays will be grown to support the insertion if
   * necessary.  The growth schedule doubles the amount
   * of allocated memory at each allocation request up until
   * the sparse array contains 1024 values, at which point
   * the growth is additive: an additional n * 1024 spaces will
   * be allocated for n in 1,2,4,8,16.  The largest amount of
   * space added to this vector will be an additional 16*1024*(sizeof(T)+4),
   * which is 196608 bytes at a type for a SparseVector[Double],
   * although more space is needed temporarily while moving to the
   * new arrays.
   */
  def update(i : Int, value : T) = {
    val offset = findOffset(i);
    if (offset >= 0) {
      // found at offset
      data(offset) = value;
    } else if (value != null) {
      // need to insert at position -(offset+1)
      val insertPos = ~offset;

      used += 1;

      var newIndex = index;
      var newData = data;

      if (used > data.length) {
        // expand array
        val newLength = {
          if (data.length < 8) { 8 }
          else if (data.length > 16*1024) { data.length + 16*1024 }
          else if (data.length > 8*1024)  { data.length +  8*1024 }
          else if (data.length > 4*1024)  { data.length +  4*1024 }
          else if (data.length > 2*1024)  { data.length +  2*1024 }
          else if (data.length > 1*1024)  { data.length +  1*1024 }
          else { data.length * 2 }
        }

        // copy existing data into new arrays
        newIndex = new Array[Int](newLength);
        newData  = new Array[T](newLength);
        System.arraycopy(index, 0, newIndex, 0, insertPos);
        System.arraycopy(data, 0, newData, 0, insertPos);
      }

      // make room for insertion
      System.arraycopy(index, insertPos, newIndex, insertPos + 1, used - insertPos - 1);
      System.arraycopy(data,  insertPos, newData,  insertPos + 1, used - insertPos - 1);

      // assign new value
      newIndex(insertPos) = i;
      newData(insertPos) = value;

      // record the insertion point
      found(i,insertPos);

      // update pointers: this is a noop if we haven't expanded the arrays
      index = newIndex;
      data = newData;
    }
  }

  /** Clears this array, resetting to the initial size. */
  def clear() {
    use(new Array[Int](initialSize), new Array[T](initialSize), 0);
  }

  /** Compacts the array by removing all stored default values. */
  def compact() {
    val nz = { // number of non-zeros
      var _nz = 0;
      var i = 0;
      while (i < used) {
        if (data(i) != default) {
          _nz += 1;
        }
        i += 1;
      }
      _nz;
    }

    val newData  = new Array[T](nz);
    val newIndex = new Array[Int](nz);

    var i = 0;
    var o = 0;
    while (i < used) {
      if (data(i) != default) {
        newData(o) = data(i);
        newIndex(o) = index(i);
        o += 1;
      }
      i += 1;
    }

    use(newIndex, newData, nz);
  }


  /** Use the given index and data arrays, of which the first inUsed are valid. */
  private def use(inIndex : Array[Int], inData : Array[T], inUsed : Int) = {
    // these rep-checks are not needed because this method is private and all
    // callers satisfy these invariants.
//    if (inIndex == null || inData == null)
//      throw new IllegalArgumentException("Index and data must be non-null");
//    if (inIndex.length != inData.length)
//      throw new IllegalArgumentException("Index and data sizes do not match");
//    if (inIndex.length < inUsed)
//      throw new IllegalArgumentException("Used is greater than provided array");
//    if (inIndex(0) < 0 || inIndex(0) >= inUsed)
//      throw new IllegalArgumentException("use inIndex out of range contains illegal offset @ 0");
//    var i = 1;
//    while (i < inUsed) {
//      if (inIndex(i) < 0 || inIndex(i) >= inUsed || inIndex(i) < inIndex(i-1))
//        throw new IllegalArgumentException("use inIndex out of range contains illegal offset @ "+i);
//      i += 1;
//    }

    data = inData;
    index = inIndex;
    used = inUsed;
    lastOffset = -1;
    lastIndex = -1;
  }

  /** Tranforms all values in this map by applying the given function. */
  def transformValues(f : T=>T) = {
    var i = 0;
    while (i < used) {
      data(i) = f(data(i));
      i += 1;
    }
  }

  override def hashCode = {
    var rv = 0;
    var i = 0;
    while (i < used) {
      if (data(i) != default) {
        rv += 17*rv + data(i).hashCode*7 + index(i);
      }
      i += 1;
    }
    rv;
  }

  override def equals(other : Any) : Boolean = other match {
    case that : SparseArray[_] =>
      var thisI = 0;
      var thatI = 0;
      while (thisI < this.used && thatI < that.used) {
        if (this.index(thisI) < that.index(thatI)) {
          if (this.data(thisI) != that.default) return false;
          thisI += 1;
        } else if (that.index(thatI) < this.index(thisI)) {
          if (that.data(thatI) != this.default) return false;
          thatI += 1;
        } else if (this.index(thisI) == that.index(thatI)) {
          if (this.data(thisI) != that.data(thatI)) return false;
          thisI += 1;
          thatI += 1;
        }
      }
      while (thisI < this.used) {
        if (this.data(thisI) != that.default) return false;
        thisI += 1;
      }
      while (thatI < that.used) {
        if (that.data(thatI) != this.default) return false;
        thatI += 1;
      }
      true;
    case _ => false;
  }
}

object SparseArray {

  def apply[@specialized T:ClassManifest:DefaultValue](length : Int, initialSize : Int = 3) =
    new SparseArray[T](length = length, initialSize = initialSize)(implicitly[DefaultValue[T]].value);

  def tabulate[@specialized T:ClassManifest:DefaultValue](length : Int, initialSize : Int = 3)(fn : (Int => T)) = {
    val rv = SparseArray[T](length = length, initialSize = initialSize);
    var i = 0;
    while (i < length) {
      val v = fn(i);
      if (v != rv.default) {
        rv(i) = v;
      }
      i += 1;
    }
    rv;
  }

  /** Default value of type T as used by SparseArray. */
  trait DefaultValue[@specialized T] {
    def value : T;
  }

  /** Default value of type T as used by SparseArray. */
  object DefaultValue {
    implicit object IntDefaultValue extends DefaultValue[Int] {
      override def value = 0; }

    implicit object LongDefaultValue extends DefaultValue[Long] {
      override def value = 0l; }

    implicit object ShortDefaultValue extends DefaultValue[Short] {
      override def value = 0.toShort; }

    implicit object CharDefaultValue extends DefaultValue[Char] {
      override def value = 0.toChar; }

    implicit object ByteDefaultValue extends DefaultValue[Byte] {
      override def value = 0.toByte; }

    implicit object FloatDefaultValue extends DefaultValue[Float] {
      override def value = 0.0f; }

    implicit object DoubleDefaultValue extends DefaultValue[Double] {
      override def value = 0.0; }
    
    implicit object BooleanDefaultValue extends DefaultValue[Boolean] {
      override def value = false; }

    implicit def ObjectDefaultValue[T<:AnyRef] = new DefaultValue[T] {
      override def value : T = null.asInstanceOf[T];
    }
  }
}

