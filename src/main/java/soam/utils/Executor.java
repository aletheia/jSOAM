/**
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package soam.utils;

import java.util.Observable;

import soam.algorithms.Algorithm;


public class Executor extends Observable implements Runnable {

    /**
     * Refresh rate
     */
    protected long UPDATE_RATE = 2000;

    /**
     * Thread sleep
     */
    protected long UPDATE_PAUSE_MSEC = 0;

    /**
     * Auto pause
     */
    protected boolean AUTO_PAUSE = false;

    /**
     * flag stopping thread
     */
    protected boolean run = true;

    /**
     * flag pausing thread
     */
    protected boolean pause = false;

    /**
     * Algorithm controller
     */
    protected Algorithm algorithm;

    protected Thread thread;

    /**
     * The constructor
     * 
     * @param algorithm
     */
    public Executor(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Run the algorithm
     * 
     */
    public void run() {

        while (run) {

            algorithm.iteration();

            // Pauses and resumes thread
            while (pause) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            if (algorithm.getTick() % UPDATE_RATE == 0) {
                setChanged();
                notifyObservers();

                // Thread sleep
                if (UPDATE_PAUSE_MSEC > 0) {
                    try {
                        Thread.sleep(UPDATE_PAUSE_MSEC);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Start running
     * 
     */
    public synchronized void start() {
        run = true;

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Resumes running
     * 
     */
    public synchronized void resume() {
        pause = false;
        notifyAll();
    }

    /**
     * Pauses running
     * 
     */
    public synchronized void pause() {
        pause = true;
        notifyAll();
    }

    /**
     * Stops running
     * 
     */
    public synchronized void stop() {
        run = false;

        // Force the execution
        resume();
    }


    public long getUPDATE_PAUSE_MSEC() {
        return UPDATE_PAUSE_MSEC;
    }

    public void setUPDATE_PAUSE_MSEC(long iteration_pause_msec) {
        if (run && !pause) {
            pause();
            UPDATE_PAUSE_MSEC = iteration_pause_msec;
            resume();
        } else {
            UPDATE_PAUSE_MSEC = iteration_pause_msec;
        }
    }

    public long getUPDATE_RATE() {
        return UPDATE_RATE;
    }

    public void setUPDATE_RATE(long update_rate) {
        if (run && !pause) {
            pause();
            UPDATE_RATE = update_rate;
            resume();
        } else {
            UPDATE_RATE = update_rate;
        }
    }
    
    public boolean isAUTO_PAUSE() {
        return AUTO_PAUSE;
    }

    public void setAUTO_PAUSE(boolean auto_pause) {
        AUTO_PAUSE = auto_pause;
    }

}