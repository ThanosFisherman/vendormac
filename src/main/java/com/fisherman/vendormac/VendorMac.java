package com.fisherman.vendormac;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;

public class VendorMac
{
    private static final String DEFAULT_SOURCE_FILE = "oui.txt";
    private static final String DEFAULT_DEST_PATH = "vendorMacs-generated.prop";
    private static final String APPLE_ENUM_PATH = "apple-enum-generated.txt";
    private static final String APPLE_MACS_CLASS = "AppleMacs.java";
    private static final String VENDOR_MACS_PATH = "vendorMacs-generated.xml";

    private static final String DELIM = "(base 16)";
    private static final String EQUAL = "=";

    private static final int MODE_URL = 0;
    private static final int MODE_FILE = 1;

    private static int MODE = MODE_URL;

    private static void printUsage()
    {
        System.out.println("Usage: ");
        System.out.println("1. url mode: java -jar vendormac.jar");
        System.out.println("2. file mode: java -jar vendormac.jar <source file>");
    }

    // '=' serated file
    private static void loadOUIAndWrite(final String source) throws IOException
    {

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_DEST_PATH)));
        String tmp;

        //bw.append("* Reference from: http://standards-oui.ieee.org/oui.txt").append("\n");
        // bw.append("*").append(br.readLine()).append("\n\n");


        while ((tmp = br.readLine()) != null)
        {
            if (!tmp.contains(DELIM))
                continue;

            StringTokenizer stk = new StringTokenizer(tmp.replace(DELIM, "="), EQUAL);
            String prefix = null;
            String vendor = null;

            if (stk.hasMoreTokens())
                prefix = stk.nextToken().trim();

            if (stk.hasMoreTokens())
                vendor = stk.nextToken().trim();

            // Make '=' separated and only get the first two words from vendor for the shake of brevity
            bw.append(prefix).append("=").append(getFirstTwo(vendor)).append("\n");
        }

        br.close();
        bw.close();

        System.out.println("Done!!!, File generated : " + DEFAULT_DEST_PATH);
    }

    private static String getFirstTwo(String original)
    {
        String arr[] = original.split(" ");
        if (arr.length >= 2)
            return arr[0] + " " + arr[1];
        else
            return arr[0];
    }

    static private final String APPLE_ENUM_PREFIX =
            "package com.yourpackage.aha.constants;\n" + "\n" + "import java.util.HashMap;\n" + "import java.util.Map;\n" + "\n" +
            "import android.annotation.SuppressLint;\n" + "\n" + "/**\n" + " * Updated " + new Date().toString() + " , \n" +
            " *  Reference from: http://standards-oui.ieee.org/oui.txt\n" + " * \n" +
            " *  Using utils/vendormac.jar, Generate enum elements from http://standards-oui.ieee.org/oui.txt\n" + " *  \n" +
            " *  OUI stands for Organizationally Unique Identifier.\n" + " *  \n" + " * NOTE: Need to Update periodically.\n" + " * \n" +
            " * @author blomdhal\n" + " * \n" + " */\n" + "@SuppressLint(\"UseSparseArrays\")\n" + "public enum AppleMacs {\n";


    static private final String APPLE_ENUM_SUFFIX = "    ;\n" + "    \n" + "    private static final Map<String, AppleMacs> kMap;\n" + "    static {\n" +
                                                    "        kMap = new HashMap<String, AppleMacs>();\n" + "        \n" +
                                                    "        for (AppleMacs k : AppleMacs.values()) {\n" + "            kMap.put(k.prefix, k);\n" +
                                                    "        }\n" + "    }\n" + "    \n" + "    public static AppleMacs getEnum(String prefix) {\n" +
                                                    "        return kMap.get(prefix);\n" + "    }\n" + "    \n" + "    public final String prefix;\n" +
                                                    "    public final String vendor;\n" + "    AppleMacs(String prefix, String vendor) {\n" +
                                                    "        this.prefix = prefix;\n" + "        this.vendor = vendor;\n" + "    }\n" + "    \n" +
                                                    "    @Override\n" + "    public String toString() {\n" + "        return new StringBuilder()\n" +
                                                    "                .append(\"prefix : \").append(prefix)\n" +
                                                    "                .append(\", vendor : \").append(vendor).toString();\n" + "    }\n" + "} // AppleMacs\n";

    private static void _loadAppleEnumElementMakeAppleEnumClass() throws IOException
    {

        BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(APPLE_ENUM_PATH)));
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(APPLE_MACS_CLASS)));
        String tmp;

        bfw.append(APPLE_ENUM_PREFIX).append("\n");

        while ((tmp = bfr.readLine()) != null)
        {

            if (!tmp.startsWith("$"))
                continue;

            // Append enum member
            bfw.append("    ").append(tmp).append("\n");
        }

        bfw.append(APPLE_ENUM_SUFFIX).append("\n");

        bfr.close();
        bfw.close();

        System.out.println("Done!!!, File generated : " + APPLE_MACS_CLASS);
    }

    private static void loadOUIAndmakeAppleMacEnumElements(final String source) throws IOException
    {

        BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(APPLE_ENUM_PATH)));
        String tmp;

        bfw.append("* Reference from: http://standards-oui.ieee.org/oui.txt").append("\n");
        bfw.append("* ").append(bfr.readLine()).append("\n\n");


        while ((tmp = bfr.readLine()) != null)
        {
            if (!tmp.contains(DELIM))
                continue;

            if (!tmp.contains("Apple"))
                continue;

            StringTokenizer stk = new StringTokenizer(tmp.replace(DELIM, "="), EQUAL);
            String prefix = null;
            String vendor = null;

            if (stk.hasMoreTokens())
                prefix = stk.nextToken().trim();

            if (stk.hasMoreTokens())
                vendor = stk.nextToken().trim();

            // Make enum member
            bfw.append("$").append(prefix).append("(\"").append(prefix).append("\", ");
            bfw.append("\"").append(vendor).append("\"),").append("\n");
        }

        bfr.close();
        bfw.close();

        System.out.println("Done!!!, File generated : " + APPLE_ENUM_PATH);
        System.out.println("You can Copy and Paste to com.lge.aha.constants.AppleMacs.java");

        _loadAppleEnumElementMakeAppleEnumClass();
    }

    private static void loadOUIAndmakeXml(final String source) throws IOException, XmlPullParserException
    {

        final String NAMESPACE = "http://standards-oui.ieee.org/oui.txt";

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        XmlSerializer serializer = factory.newSerializer();

        BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(VENDOR_MACS_PATH)));
        String tmp;

        // xml
        serializer.setOutput(bfw);
        serializer.startDocument(null, Boolean.TRUE);
        serializer.ignorableWhitespace("\n");
        serializer.comment(bfr.readLine());
        serializer.ignorableWhitespace("\n");

        serializer.setPrefix("", NAMESPACE);
        serializer.startTag(NAMESPACE, "MacAddressVendorMappings");
        //serializer.text("\n");

        while ((tmp = bfr.readLine()) != null)
        {
            if (!tmp.contains(DELIM))
                continue;

            StringTokenizer stk = new StringTokenizer(tmp.replace(DELIM, "="), EQUAL);
            String prefix = null;
            String vendor = null;

            if (stk.hasMoreTokens())
                prefix = stk.nextToken().trim();

            if (stk.hasMoreTokens())
                vendor = stk.nextToken().trim();

            // xml attribute
            writeLine(serializer, prefix, getFirstTwo(vendor));
        }

        serializer.endTag(NAMESPACE, "MacAddressVendorMappings");
        serializer.endDocument();

        //write xml data into the FileOutputStream
        serializer.flush();

        bfr.close();
        bfw.close();

        System.out.println("Done!!!, File generated : " + VENDOR_MACS_PATH);
    }

    private static void writeLine(XmlSerializer serializer, String prefix, String vendor) throws IOException
    {

        // set indentation option
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "VendorMapping");
        serializer.attribute(null, "mac_prefix", prefix);
        serializer.attribute(null, "vendor_name", vendor.replaceAll("&", ""));
        serializer.endTag(null, "VendorMapping");
        //        serializer.text("\n");
    }

    private static void downloadouitxt() throws IOException
    {
        System.out.println("Downloading oui file... from http://standards-oui.ieee.org/oui.txt");

        int size;

        URL url = new URL("http://standards-oui.ieee.org/oui.txt");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        size = conn.getContentLength();

        System.out.println("url = " + url);
        System.out.println("protocol = " + url.getProtocol());
        System.out.println("host = " + url.getHost());
        System.out.println("content = " + url.getContent());
        System.out.println("size = " + size + "bytes");
        System.out.println("");

        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buff = new byte[2048];
        int read;
        int progress = -1;
        int total = 0;

        long begin = System.currentTimeMillis();

        while ((read = bis.read(buff)) != -1)
        {
            total += read;
            int cur = (total * 100) / size;

            if (cur != progress)
            {
                progress = cur;
                int elasped = (int) ((System.currentTimeMillis() - begin) / 1000);
                System.out.println(String.format("%7d bytes / %7d bytes (%3d percent), %3ds elasped.", total, size, progress, elasped));
            }
            baos.write(buff, 0, read);
        }

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(DEFAULT_SOURCE_FILE));
        bos.write(baos.toByteArray());
        bos.close();
        bis.close();
        baos.close();
        conn.disconnect();

        System.out.println("download done!!!, total elasped: " + ((System.currentTimeMillis() - begin) / 1000) + "s.");
    }

    /**
     * @param args - arguments
     * @throws IOException            - When files cannot be created
     * @throws XmlPullParserException - When XML file cannot be created
     */
    public static void main(String[] args) throws XmlPullParserException, IOException
    {

        String ouiFile = null;

        System.out.println("Generate vendor mac file from RAW IEEE-oui files(http://standards-oui.ieee.org/oui.txt)");
        System.out.println("");

        if (args.length > 1)
        {
            printUsage();
            return;
        }

        if (args.length == 0)
            MODE = MODE_URL;
        else
            MODE = MODE_FILE;

        switch (MODE)
        {
            case MODE_URL:
                try
                {
                    ouiFile = DEFAULT_SOURCE_FILE;
                    downloadouitxt();
                }
                catch (IOException e)
                {
                    System.out.println("Check your internet connection...");
                    e.printStackTrace();
                }
                break;
            case MODE_FILE:
                if (new File(args[0]).isFile())
                    ouiFile = args[0];
                else
                {
                    System.out.println(args[0] + " does not exist. use default source file: " + DEFAULT_SOURCE_FILE);
                    ouiFile = DEFAULT_SOURCE_FILE;
                }
                break;
        }

        System.out.println("");

        loadOUIAndWrite(ouiFile);
        loadOUIAndmakeAppleMacEnumElements(ouiFile);
        loadOUIAndmakeXml(ouiFile);
    }

}
