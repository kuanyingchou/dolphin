package api.util;

public class Pair<T, E> {
   private T left;
   private E right;
   public Pair(T t, E e) {
      left=t;
      right=e;
   }
   public T getLeft() { return left; }
   public E getRight() { return right; }
}
