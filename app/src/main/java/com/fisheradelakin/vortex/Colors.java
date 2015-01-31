package com.fisheradelakin.vortex;

import android.graphics.Color;

import java.util.Random;

/**
 * Created by Fisher on 1/26/15.
 */
public class Colors {
    public String[] mColors = {
            "#1abc9c", // turquoise
            "#2ecc71", // emerald
            "#3498db", // peter river
            "#9b59b6", // amethyst
            "#34495e", // wet asphalt
            "#16a085", // green sea
            "#27ae60", // nephritis
            "#2980b9", // belize hole
            "#8e44ad", // wisteria
            "#2c3e50", // midnight blue
            "#f1c40f", // sunflower
            "#e67e22", // carrot
            "#e74c3c", // alizarin
            "#95a5a6", // concrete
            "#f39c12", // orange
            "#d35400", // pumpkin
            "#c0392b", // pomegranate
            "#7f8c8d", // asbestos
            "#39add1", // light blue
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#637a91", // dark gray
            "#f092b0", // pink
            "#b7c0c7"  // light gray

    };

    // Randomly select a color
    static Random rand = new Random();

    public int darkColor() {
        Color.alpha(mColors.length);
        return Color.parseColor(mColors[rand.nextInt(mColors.length)]);
    }

    public int getColor() {
        return Color.parseColor(mColors[rand.nextInt(mColors.length)]);
    }
}
