package com.ebook.epub.viewer;


import android.util.Log;

public class DebugSet {
    
    public static boolean DEBUGABLE = true;    // TODO :: release는 false;

    private static void printD(String _s1, String _s2) {
        if( DEBUGABLE ) {
            Log.d( _s1 , _s2 );
        }
    }
    private static void printE(String _s1, String _s2) {
        if( DEBUGABLE ) {
//            Log.e( _s1 , _s2 );
        }
    }
    private static void printI(String _s1, String _s2) {
        if( DEBUGABLE ) {
//            Log.i( _s1 , _s2 );
        }
    }
    private static void printW(String _s1, String _s2) {
        if( DEBUGABLE ) {
//            Log.w( _s1 , _s2 );
        }
    }

    public static void d( String _s1 , String _s2 )
    {
        try {
            if( _s2 == null )
                throw new Exception();

            _s2 = String.format("************************ %s", _s2);
            printD(_s1, _s2);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void e( String _s1 , String _s2 )
    {
        try {
            if( _s2 == null )
                throw new Exception();

            printE(_s1, _s2);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void i( String _s1 , String _s2 )
    {
        try {
            if( _s2 == null )
                throw new Exception();

            printI(_s1, _s2);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void w( String _s1 , String _s2 )
    {
        try {
            if( _s2 == null )
                throw new Exception();

            printW(_s1, _s2);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void printMemory(String tag) {
//        DebugSet.i("Memory",
//                tag + " : "
//                    + "---------------------------------------------------------- ");
//        DebugSet.i("Memory",
//                tag + " : " + " max " + ((Runtime.getRuntime().maxMemory())));
//        DebugSet.i("Memory",
//                tag + " : " + " total " + (Runtime.getRuntime().totalMemory()));
//        DebugSet.i("Memory",
//                tag + " : " + " free " + (Runtime.getRuntime().freeMemory()));
//        DebugSet.i("Memory", tag
//                + " : "
//                + " total - free "
//                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
//                        .freeMemory()));
//        DebugSet.i("Memory",
//                tag
//                + " : "
//                + "---------------------------------------------------------- ");
//        DebugSet.i("Memory", tag + " : " + " getNativeHeapSize "
//                + Debug.getNativeHeapSize());
//        DebugSet.i("Memory", tag + " : " + " getNativeHeapFreeSize "
//                + Debug.getNativeHeapFreeSize());
//        DebugSet.i(
//                "Memory",
//                tag + " : " + " getNativeHeapAllocatedSize "
//                        + Debug.getNativeHeapAllocatedSize());
//        DebugSet.i("Memory",
//                tag
//                + " : "
//                + " --------------------------------------------------------- ");
    }
}
