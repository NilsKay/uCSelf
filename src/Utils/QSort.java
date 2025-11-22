package Utils;
import SOUND.OneNote;

public class QSort {

    /** 
     * Sort 2 arrays using one to decide how to sort a[]
    * @param a       a double array
    * @param b	     a corresponding int array (same size !)
    * @param lo0     left boundary of array partition
    * @param hi0     right boundary of array partition
    */
   public void quickSort(double a[], OneNote b[], int lo0, int hi0) {
      int lo = lo0;
      int hi = hi0;
      double mid;
      if ( hi0 > lo0) {
         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = a[ ( lo0 + hi0 ) / 2 ];

         // loop through the array until indices cross
         while( lo <= hi ) {
            /* find the first element that is greater than or equal to 
             * the partition element starting from the left Index.
             */
	     while( ( lo < hi0 ) && ( a[lo] < mid ))
		 ++lo;

            /* find an element that is smaller than or equal to 
             * the partition element starting from the right Index.
             */
	     while( ( hi > lo0 ) && ( a[hi] > mid ))
		 --hi;

            // if the indexes have not crossed, swap
            if( lo <= hi ) {
               swap(a, b, lo, hi);
               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if( lo0 < hi )
            quickSort( a, b, lo0, hi );

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if( lo < hi0 )
            quickSort( a, b, lo, hi0 );

      }
   }

   private void swap(double a[], OneNote b[], int i, int j) {
      OneNote T;
      double dT;
      dT = a[i]; 
      T = b[i]; 
      a[i] = a[j];
      b[i] = b[j];
      a[j] = dT;
      b[j] = T; 
   }

   public void sort(double a[], OneNote b[]) {
      quickSort(a, b, 0, a.length - 1);
   }
}





