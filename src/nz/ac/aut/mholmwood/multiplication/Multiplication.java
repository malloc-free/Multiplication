package nz.ac.aut.mholmwood.multiplication;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import nz.ac.aut.ada.graphing.data.GraphingData;

public class Multiplication {
        
        
    public static final int MIN_N = 10;
    public static final int MAX_N = 500;
    public static final int NUM_ITERATIONS = 10;
    /**
     * The main method performs a test run, to ensure the algorithm's are
     * working. Then we generate the data to be mapped. Sorry its all a 
     * bit procedural, decided against breaking it down. The main bits 
     * (the algorithms) work though.
     * 
     * @param args 
     */
    public static void main (String[] args){

        //Just a quick test to make sure everything is running ok.
        BigInteger x = new BigInteger("125");
        BigInteger y = new BigInteger("690");
        System.out.printf("\nX value = %s", x.toString());
        System.out.printf("\nY value = %s", y.toString());
        BigInteger res = karatsuba(x, y);
        BigInteger resTwo = oldSchool(x, y);
        BigInteger resThree = karatsubaMultiThread(x, y);
        System.out.printf("\nResult karatsuba = %s", res.toString());
        System.out.printf("\nResult oldSchool = %s", resTwo.toString());
        System.out.printf("\nResult direct    = %s", x.multiply(y));
        System.out.printf("\nResult multi     = %s\n", resThree);

        //Now for the real work. A big lump of junk, but it works.
        Random random = new SecureRandom();
        Long time;
        int[] xVals = new int[MAX_N - MIN_N + 1];
        int[] yKSVals = new int[MAX_N - MIN_N + 1];
        int[] yOSVals = new int[MAX_N - MIN_N + 1];
        int[] yMTVals = new int[MAX_N - MIN_N + 1];
        
        for(int n = MIN_N; n <= MAX_N; n++){
            xVals[n - MIN_N] = n;
            
            x = new BigInteger(n, random);
            y = new BigInteger(n, random);
            
            
            time = System.currentTimeMillis();
            
            for(int t = 0; t < NUM_ITERATIONS; t++){
                karatsuba(x, y);
            }
            
            time = System.currentTimeMillis() - time;
            yKSVals[n - MIN_N] = time.intValue();
            
            time = System.currentTimeMillis();
            
            for(int t = 0; t < NUM_ITERATIONS; t++){
                oldSchool(x, y);
            }
            
            time = System.currentTimeMillis() - time;
            yOSVals[n - MIN_N] = time.intValue();
            
            time = System.currentTimeMillis();
            
            for(int t = 0; t < NUM_ITERATIONS; t++){
                karatsubaMultiThread(x, y);
            }
            
            time = System.currentTimeMillis() - time;
            yMTVals[n - MIN_N] = time.intValue();
            
        }
        
        GraphingData.createGraph(xVals, yKSVals, "Karatsuba");
        GraphingData.createGraph(xVals, yOSVals, "OldSchool");
        GraphingData.createGraph(xVals, yMTVals, "MultiThread");
        
        //Test our multi-threaded karatsuba with some really big numbers
        x = new BigInteger(8192, random);
        y = new BigInteger(8192, random);
        
        time = System.currentTimeMillis();
        
        for(int t = 0; t < 10; t++){
            karatsuba(x, y);
        }
        
        System.out.printf("Kara t = %s\n", System.currentTimeMillis() - time);
        
        time = System.currentTimeMillis();
        
        for(int t = 0; t < 10; t++){
            karatsubaMultiThread(x, y);
        }
        
        System.out.printf("Kara multi t = %s\n", System.currentTimeMillis() - time);
    }
    
    /**
     * Karatsuba's algorithm, multiplies two BigIntegers.
     * 
     * @param x - The first number to multiply.
     * @param y - The second number to multiply.
     * @return - The product of the two number supplied.
     */
    public static BigInteger karatsuba(BigInteger x, BigInteger y){
        BigInteger retVal;

        //Find the longer of the two values, use length of that.
        int n = (x.bitLength() > y.bitLength()) ? x.bitLength() : y.bitLength();

        //Multiply directly if bit length <= 1.
        if(n <= 1){
            retVal = x.multiply(y);
        }
        else{
            //find n/2 ceiling.
            n = (n / 2) + n % 2;

            //Split the numbers.
            BigInteger a = x.shiftRight(n);
            BigInteger b = x.subtract(a.shiftLeft(n));
            BigInteger c = y.shiftRight(n);
            BigInteger d = y.subtract(c.shiftLeft(n));

            //The three multiplications.
            BigInteger p1 = karatsuba(a, c);
            BigInteger p2 = karatsuba(b, d);
            BigInteger p3 = karatsuba(a.add(b), c.add(d));

            //Subtract and shift.
            p3 = p3.subtract(p2);
            p3 = p3.subtract(p1);
            p3 = p3.shiftLeft(n);
            p1 = p1.shiftLeft(n * 2);

            //Final additions.
            retVal = p1.add(p2).add(p3);
        }

        return retVal;
    }

    /**
     * A multi-threaded version of karatsubas algorithm. Just because.
     * 
     * @param x
     * @param y
     * @return 
     */
    public static BigInteger karatsubaMultiThread(BigInteger x, BigInteger y){
         BigInteger retVal;

        //Find the longer of the two values, use length of that.
        int n = (x.bitLength() > y.bitLength()) ? x.bitLength() : y.bitLength();

        //Multiply directly if bit length <= 1.
        if(n <= 1){
            retVal = x.multiply(y);
        }
        else{
            //find n/2 ceiling.
            n = (n / 2) + n % 2;

            //Split the numbers. 
            final BigInteger a = x.shiftRight(n);
            final BigInteger b = x.subtract(a.shiftLeft(n));
            final BigInteger c = y.shiftRight(n);
            final BigInteger d = y.subtract(c.shiftLeft(n));
            
            MK p1 = new MK(){{ x = a; y = c; }};
            MK p2 = new MK(){{ x = b; y = d; }};
            MK p3 = new MK(){{ x = a.add(b); y = c.add(d); }};
            
            Thread tA = new Thread(p1);
            Thread tB = new Thread(p2);
            Thread tC = new Thread(p3);
            
            tA.start();
            tB.start();
            tC.start();
            
            try{
                tA.join();
                tB.join();
                tC.join();
            } catch(InterruptedException e){
                System.out.print("Interrupted, exiting");
                System.exit(-1);
            }

            //Subtract and shift.
            p3.result = p3.result.subtract(p2.result);
            p3.result = p3.result.subtract(p1.result);
            p3.result = p3.result.shiftLeft(n);
            p1.result = p1.result.shiftLeft(n * 2);

            //Final additions.
            retVal = p1.result.add(p2.result).add(p3.result);
        }

        return retVal;
    }
    
    public static class MK implements Runnable{
        
        BigInteger result;
        BigInteger x;
        BigInteger y;

        @Override
        public void run() {
            result = karatsuba(x, y);
        }
    }
    
    /**
     * School long multiplication algorithm, multiplies two BigIntegers.
     * 
     * @param x - The first number to multiply.
     * @param y - The second number to multiply.
     * @return - The product of the two numbers.
     */
    public static BigInteger oldSchool(BigInteger x, BigInteger y){
        BigInteger retVal;

        //Find the longer of the two values, use that.
        int n = (x.bitLength() > y.bitLength()) ? x.bitLength() : y.bitLength();

        //Muliply directly when bit length <= 1.
        if(n <= 1){
            retVal = x.multiply(y);
        }
        else{
            //Find n/2 ceiling.
            n = (n / 2) + n % 2;

            //Split the numbers
            BigInteger a = x.shiftRight(n);
            BigInteger b = x.subtract(a.shiftLeft(n));
            BigInteger c = y.shiftRight(n);
            BigInteger d = y.subtract(c.shiftLeft(n));

            //The four multiplications
            BigInteger ac = oldSchool(a, c);
            BigInteger ad = oldSchool(a, d);
            BigInteger bc = oldSchool(b, c);
            BigInteger bd = oldSchool(b, d);

            //Shift and add
            ac = ac.shiftLeft(n * 2);
            BigInteger adPlusBc = ad.add(bc);
            adPlusBc = adPlusBc.shiftLeft(n);

            //The final additions
            retVal = ac.add(adPlusBc).add(bd);
        }

        return retVal;
    }
}
