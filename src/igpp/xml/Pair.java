package igpp.xml;

/**
 * An simple class to maintain pairs of objects.
 * Adapted form a web post.
 *
 * @version     1.0.0
 * @since     1.0.0
 **/
public class Pair<L, R> {
 
	private L left;
	private R right;
 
	/** 
	* Creates an instance of pair with a "left" value and a "right" value. 
	*
	* @since           1.0
	**/
	public Pair(final L left, final R right) {
	  this.left = left;
	  this.right = right;
	}
    
	/** 
	* Creates an instance of pair with a "left" value and a "right" value. 
	* The type of each value can be defined.
	*
	* @since           1.0
	**/
	public static <A, B> Pair<A, B> create(A left, B right) {
	  return new Pair<A, B>(left, right);
	}
 
	/** 
	* Returns the "right" element of the pair. 
	*
	* @since           1.0
	**/
	public R getRight() {
	  return right;
	}
 
	/** 
	* Returns the "left" element of the pair. 
	*
	* @since           1.0
	**/
	public L getLeft() {
	  return left;
	}
 
	/** 
	* Sets the "right" element of the pair. 
	*
	* @param value     the value to set the element.
	* 
	* @since           1.0
	**/
	public void setRight(R value) {
	  right = value;
	}
 
	/** 
	* Set the "left" element of the pair. 
	*
	* @param value	the value to set the element.
	*
	* @since           1.0
	**/
	public void setLeft(L value) {
	  left = value;
	}
	
	/** 
	* Determines if one object equals another. 
	* This checks if the content of each object
	* is the same. 
	*
   * @return          <code>true</code> if the content is the same;
   *                  <code>false</code> otherwise.
   *
	* @since           1.0
	**/
	public final boolean equals(Object o) {
	  if (!(o instanceof Pair))
	      return false;
	
	  final Pair<?, ?> other = (Pair<?, ?>) o;
	  return equal(getLeft(), other.getLeft()) && equal(getRight(), other.getRight());
	}
	
	/** 
	* Determines if one object equals another. 
	* This checks if the content of each object
	* is the same. 
	*
   * @return          <code>true</code> if the content is the same;
   *                  <code>false</code> otherwise.
   *
	* @since           1.0
	**/
	public static final boolean equal(Object o1, Object o2) {
	  if (o1 == null) {
	      return o2 == null;
	  }
	  return o1.equals(o2);
	}
	
	/** 
	* Returns the hash code. 
	* This checks if the content of each object
	* is the same. 
	*
   * @return          the hash code value.
   *
	* @since           1.0
	**/
	public int hashCode() {
	  int hLeft = getLeft() == null ? 0 : getLeft().hashCode();
	  int hRight = getRight() == null ? 0 : getRight().hashCode();
	
	  return hLeft + (57 * hRight);
	}
}

