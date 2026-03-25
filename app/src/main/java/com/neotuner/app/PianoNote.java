package com.neotuner.app;

public class PianoNote {
    private static double a4Freq = 440.0;
    private static final double SEMITONE_RATIO = Math.pow(2, 1.0 / 12);
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static class Note {
        public final String name;
        public final int octave;
        public final double frequency;
        public final int midiNumber;

        public Note(String name, int octave, double frequency, int midiNumber) {
            this.name = name;
            this.octave = octave;
            this.frequency = frequency;
            this.midiNumber = midiNumber;
        }

        public String getFullName() {
            return name + octave;
        }
    }

    public static void setA4Frequency(double frequency) {
        a4Freq = frequency;
    }

    public static double getA4Frequency() {
        return a4Freq;
    }

    public static Note findClosestNote(double frequency) {
        if (frequency <= 0) {
            return null;
        }

        double centsFromA4 = 1200 * Math.log(frequency / a4Freq) / Math.log(2);
        int nearestSemitones = (int) Math.round(centsFromA4 / 100);
        int midiNumber = 69 + nearestSemitones;

        if (midiNumber < 21 || midiNumber > 108) {
            return null;
        }

        double noteFrequency = a4Freq * Math.pow(SEMITONE_RATIO, nearestSemitones);
        int noteIndex = (midiNumber - 21) % 12;
        int octave = (midiNumber - 12) / 12;

        return new Note(NOTE_NAMES[noteIndex], octave, noteFrequency, midiNumber);
    }

    public static double getCentsDifference(double frequency, double targetFrequency) {
        if (frequency <= 0 || targetFrequency <= 0) {
            return 0;
        }
        return 1200 * Math.log(frequency / targetFrequency) / Math.log(2);
    }

    public static Note getNoteByMidi(int midiNumber) {
        if (midiNumber < 21 || midiNumber > 108) {
            return null;
        }

        int semitonesFromA4 = midiNumber - 69;
        double frequency = a4Freq * Math.pow(SEMITONE_RATIO, semitonesFromA4);
        int noteIndex = (midiNumber - 21) % 12;
        int octave = (midiNumber - 12) / 12;

        return new Note(NOTE_NAMES[noteIndex], octave, frequency, midiNumber);
    }
}
