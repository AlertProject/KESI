/*
 * Copyright (C) 2011-2013 GSyC/LibreSoft
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Authors: Santiago Dueñas <sduenas@libresoft.es>
 *
 */

package eu.alertproject.kesi.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class CommandRunner {

    static Logger logger = Logger.getLogger(CommandRunner.class);

    private class ThreadStreamReader extends Thread {
        InputStream is;
        OutputStream os;
        String tag;

        public ThreadStreamReader(InputStream input, OutputStream output,
                String tag) {
            this.is = input;
            this.os = output;
            this.tag = tag;
        }

        @Override
        public void run() {
            PrintWriter pw = new PrintWriter(os);

            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String line = null;

                while ((line = br.readLine()) != null) {
                    pw.println("[" + tag + "] " + line);
                }

                pw.flush();
                pw.close();

            } catch (IOException e) {
                e.printStackTrace(pw);
            }
        }
    }

    public int run(String tool, String[] cmd, String dirname) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        String cmdline = "Running " + tool + ":";

        for (String arg : cmd) {
            cmdline += " " + arg;
        }

        logger.info(cmdline);

        try {
            int code;
            Process process;

            if (dirname != null) {
                File dir = new File(dirname);
                process = Runtime.getRuntime().exec(cmd, null, dir);
            } else {
                process = Runtime.getRuntime().exec(cmd);
            }

            ThreadStreamReader stdoutReader = new ThreadStreamReader(
                    process.getInputStream(), stdout, tool + " - MSG");
            ThreadStreamReader stderrReader = new ThreadStreamReader(
                    process.getErrorStream(), stderr, tool + " - ERROR");

            stdoutReader.start();
            stderrReader.start();

            code = process.waitFor();
            stdoutReader.join();
            stderrReader.join();

            logger.info(stdout.toString());
            logger.error(stderr.toString());

            return code;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
