/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package z_soundapi;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Dr. Abdourahmane Senghor
 */
public class onlineSoundRecordStopPlay {

    TargetDataLine targetLine;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ArrayList<Byte> soundArrayByte = new ArrayList();

    class RunRecord implements Runnable {

        //For Online Sound Transfer:start       
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        @Override
        public void run() {
            out = new ByteArrayOutputStream();
            soundArrayByte = new ArrayList<>();
            try {
                targetLine = (TargetDataLine) AudioSystem.getLine(info);
                targetLine.open();
            } catch (LineUnavailableException ex) {

            }
            targetLine.start();
            byte[] data = new byte[targetLine.getBufferSize() / 5];
            int readBytes = 1;
            while (readBytes > 0) {
                readBytes = targetLine.read(data, 0, data.length);
                out.write(data, 0, readBytes);
                ///out.flush();
                ////System.out.println("Running Record..., Thread State:  " + Thread.currentThread().getId() + "   " + Thread.currentThread().isInterrupted());
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }

            System.out.println("Ended Recording  ");
            byte[] soundarraybte = out.toByteArray();
            for (int i = 0; i < soundarraybte.length; i++) {
                soundArrayByte.add(soundarraybte[i]);
            }
        }
    }

    class LauchingSignalRecord implements Runnable {
        @Override
        public void run() {
            synchronized (lock_signal) {
                STARTSIGNAL = 1;
                lock_signal.notify();
            }
        }
    }

    class BlockingRecord implements Runnable {
        @Override
        public void run() {
            //System.out.println("BEFORE-WAITING - Thread.currentThread().getState(): " + Thread.currentThread().getState());
            synchronized (lock_signal) {
                while (STARTSIGNAL == 0) {
                    try {
                        ///System.out.println("WHILE-WAITING -Thread.currentThread().getState(): " + Thread.currentThread().getState());
                        lock_signal.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                ////threadSoundRecord.interrupt();
                ////System.out.println("AFTER-WAITING -Thread.currentThread().getState(): " + Thread.currentThread().getState());
                runRecord.run();
            }
        }
    }
    BlockingRecord blockingRecord = new BlockingRecord();
    Thread threadblockingRecord = new Thread(blockingRecord);
    RunRecord runRecord = new RunRecord();
    LauchingSignalRecord launchingRecord = new LauchingSignalRecord();
    Thread threadSoundRecord = new Thread(runRecord);
    Thread threadSoundRecordlaunching = new Thread(launchingRecord); //notify le thread enregistrement de record de sortir du wait
    int STARTSIGNAL = 0;
    final Object lock_signal = new Object();
}
