/* Merchant of Venice - technical analysis software for the stock market.
 Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
package org.mov.prefs;
import org.mov.prefs.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Provides the function of handle authentication requests in proxy.
 * 
 * @author Bryan Lin 2004-9-11
 */
public class ProxyAuthenticator extends Authenticator {
    PreferencesManager.ProxyPreferences proxyPreferences = PreferencesManager
            .loadProxySettings();

    protected PasswordAuthentication getPasswordAuthentication() {
        String promptString = getRequestingPrompt();
        String hostname = getRequestingHost();
        InetAddress ipaddr = getRequestingSite();
        int port = getRequestingPort();
        String username = proxyPreferences.user;
        String password = proxyPreferences.password;
        return new PasswordAuthentication(username, password.toCharArray());
    }

    /**
     * Setup the networking to handle authentication requests and work http
     * proxies correctly
     */
    public static void setupNetworking() {
        PreferencesManager.ProxyPreferences proxyPreferences = PreferencesManager
                .loadProxySettings();
        if (proxyPreferences.isEnabled) {
            String host = proxyPreferences.host;
            String port = proxyPreferences.port;
            System.getProperties().put("http.proxyHost", host);
            System.getProperties().put("http.proxyPort", port);
            // this will deal with any authentication requests properly
            if (proxyPreferences.authEnabled) {
                java.net.Authenticator.setDefault(new ProxyAuthenticator());
            }
        }
    }

    public static void main(String args[]) {
        setupNetworking();

        //for test the ProxyAuthenticator
        String s = "600058.SS";//.toLowerCase();
        String a = "04", b = "10", c = "2004";//Start Date: 10-May-2004
        String d = "04", e = "13", f = "2004";//End Date: 13-May-2004
        StringBuffer r = new StringBuffer(
                "http://ichart.finance.yahoo.com/table.csv"
        //+"?s=600058.SS&a=08&b=1&c=2004&d=08&e=7&f=2004&g=d&ignore=.csv"
        );
        r.append("?s=").append(s);
        r.append("&a=").append(a);
        r.append("&b=").append(b);
        r.append("&c=").append(c);
        r.append("&d=").append(d);
        r.append("&e=").append(e);
        r.append("&f=").append(f);
        r.append("&g=").append("d");
        r.append("&ignore=.csv");
        try {
            /* Yahoo uses English locale for date format... force the locale */
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy",
                    Locale.ENGLISH);
            URLConnection connection = new URL(r.toString()).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String l = in.readLine();
            // make sure that we have valid data format.
            if (!l.equals("Date,Open,High,Low,Close,Volume,Adj. Close*")) {
                in.close();
                return;
            }
            // 8-Apr-03,25.31,25.83,25.20,25.58,54132100,25.44
            l = in.readLine();
            while (l != null) {
                if (!l.startsWith("<")) { // may have comments in file
                    String[] fields = l.split(",");
                    Date date = df.parse(fields[0]);
                    BigDecimal high = new BigDecimal(fields[2]);
                    BigDecimal low = new BigDecimal(fields[3]);
                    BigDecimal close = new BigDecimal(fields[4]);
                    long volume = Long.parseLong(fields[5]);
                    System.out.print("Date:" + date + " ");
                    System.out.print("C:" + close.floatValue() + " ");
                    System.out.print("V:" + volume + " ");
                    System.out.print("H:" + high.floatValue() + " ");
                    System.out.println("L:" + low.floatValue() + " ");
                }
                l = in.readLine();
            }
            // close everything up
            in.close();
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        } catch (Exception ex) {
            Logger.global.severe(ex.toString());
            ex.printStackTrace();
        }
    }
}