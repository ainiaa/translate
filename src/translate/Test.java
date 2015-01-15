/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translate;

/**
 *
 * @author Administrator
 */
public class Test {
    public static void main(String[] args) {
//        System.out.println("Fleurs de Noël");
        String testStr = "abc123";
        String[] testStrArr = testStr.split("@");
        for(String str : testStrArr) {
            System.out.println(str);
        }
    }
}
