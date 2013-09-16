/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class MySocket {

    public static void main(String args[]) {

        try {
            Socket socket = new Socket("172.17.0.80", 6001);
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readline;
            System.out.println("is.readLine() before");
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            while (true) {
                os.println("login\r\n");
                os.flush();
                readline = is.readLine();
                System.out.println("readline:" + readline);
                System.out.println("Server:" + is.readLine());
            } //继续循环
        } catch (Exception e) {
            System.out.println("Error" + e); //出错，则打印出错信息
        }

    }
}
