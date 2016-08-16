package org.infinispan.objectfilter.impl.util;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author anistor@redhat.com
 * @since 7.0
 */
public class ReflectionHelperTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   // start of test dummies

   private static class Base {

      private int prop1;

      public float getProp2() {
         return 0;
      }

      private Base prop3 = null;
   }

   private abstract static class X {
   }

   private abstract static class Y extends X implements Comparable<String>, List<Long> {
   }

   private abstract static class Z extends Y {
   }

   private abstract static class Q extends X implements Map<String, Double> {
   }

   private abstract static class W extends Q {
   }

   @SuppressWarnings("unused")
   private static class A<T extends Base> {
      T[] array;
      Float[] array2;
      float[] array3;
      Collection<T>[] array4;
      List<Integer> list;
      List<List<Integer>> list2;
      List<Base> list3;
      Map<String, Integer> map;
      Map<T, T> map2;
      Y y;
      Z z;
      Q q;
      Q w;
   }

   // end of dummies

   @Test
   public void testGetSimpleProperty() throws Exception {
      assertEquals(int.class, ReflectionHelper.getAccessor(Base.class, "prop1").getPropertyType());
      assertEquals(float.class, ReflectionHelper.getAccessor(Base.class, "prop2").getPropertyType());
   }

   @Test
   public void testPropertyNotFound() throws Exception {
      expectedException.expect(IntrospectionException.class);
      expectedException.expectMessage("Property not found: unknown");
      ReflectionHelper.getAccessor(Base.class, "unknown");
   }

   @Test
   public void testGetNestedProperty() throws Exception {
      ReflectionHelper.PropertyAccessor prop3 = ReflectionHelper.getAccessor(Base.class, "prop3");
      assertEquals(Base.class, prop3.getPropertyType());
      assertEquals(int.class, prop3.getAccessor("prop1").getPropertyType());
   }

   @Test
   public void testGetMultipleProperty() throws Exception {
      assertEquals(Base.class, ReflectionHelper.getAccessor(A.class, "array").getPropertyType());
      assertEquals(Float.class, ReflectionHelper.getAccessor(A.class, "array2").getPropertyType());
      assertEquals(float.class, ReflectionHelper.getAccessor(A.class, "array3").getPropertyType());
      assertEquals(Collection.class, ReflectionHelper.getAccessor(A.class, "array4").getPropertyType());

      assertEquals(Integer.class, ReflectionHelper.getAccessor(A.class, "list").getPropertyType());
      assertEquals(List.class, ReflectionHelper.getAccessor(A.class, "list2").getPropertyType());
      assertEquals(Base.class, ReflectionHelper.getAccessor(A.class, "list3").getPropertyType());

      assertEquals(Integer.class, ReflectionHelper.getAccessor(A.class, "map").getPropertyType());
      assertEquals(Base.class, ReflectionHelper.getAccessor(A.class, "map2").getPropertyType());

      assertEquals(Long.class, ReflectionHelper.getAccessor(A.class, "y").getPropertyType());
      assertEquals(Long.class, ReflectionHelper.getAccessor(A.class, "z").getPropertyType());
      assertEquals(Double.class, ReflectionHelper.getAccessor(A.class, "q").getPropertyType());
      assertEquals(Double.class, ReflectionHelper.getAccessor(A.class, "w").getPropertyType());
   }
}
