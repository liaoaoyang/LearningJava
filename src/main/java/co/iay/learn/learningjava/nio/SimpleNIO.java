package co.iay.learn.learningjava.nio;


public class SimpleNIO {
    final public static int    BUFFER_SIZE                       = 16;
    final public static int    MODE_NONE                         = 0;
    final public static int    MODE_READ                         = 1;
    final public static int    MODE_WRITE                        = 2;
    final public static int    MODE_WRITE_PREPARE                = 3;
    final public static int    MODE_WRITE_FINSHED                = 4;
    final public static int    MODE_READ_PREPARE                 = 5;
    final public static int    MODE_READ_FINSHED                 = 6;
    final public static String BUFFER                            = "BUFFER";
    final public static String SOCKET_CHANNEL                    = "SOCKET_CHANNEL";
    final public static String SERVER_SOCKET_CHANNEL             = "SERVER_SOCKET_CHANNEL";
    final public static long   DEFAULT_CLIENT_IDEL_MS_THRESHOULD = 5000L;
    final public static long   DEFAULT_MAX_CLIENT_ID             = 100000000000L;
    final public static long   DEFAULT_DISPLAY_INTERVAL_MS       = 2000;
}
