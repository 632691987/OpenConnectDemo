package org.openconnect.oauth2.server.demo;

import java.lang.reflect.Field;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.misc.Unsafe;

@SpringBootApplication
public class OAuth2ServerApplication {

  /**
   * This function is only for remove default illegal warning in JDK 11 when programme boot start
   */
  public static void disableWarning() {
      try {
          Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
          theUnsafe.setAccessible(true);
          Unsafe u = (Unsafe) theUnsafe.get(null);

          Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
          Field logger = cls.getDeclaredField("logger");
          u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
      } catch (Exception e) {

      }
  }

  public static void main(String[] args) {
    disableWarning();
    SpringApplication.run(OAuth2ServerApplication.class, args);
  }
}
