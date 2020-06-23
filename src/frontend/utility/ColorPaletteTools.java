package frontend.utility;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Text;

public class ColorPaletteTools {
    private PhongMaterial NaNMaterial;
    private PhongMaterial[] materials;
    private int colorRange;
    private float opacity;

    public ColorPaletteTools(int colorRange, float opacity, PhongMaterial NaNMaterial) {
        this.colorRange = colorRange;
        this.opacity = opacity;
        this.NaNMaterial = NaNMaterial;
        materials = new PhongMaterial[colorRange * 2];
        createPalette();
    }

    private void createPalette(){
        Color positiveMaxColor = new Color(0.8, 0, 0, opacity);
        Color positiveMinColor = new Color(0.5, 0.5, 0, opacity);
        Color negativeMinColor = new Color(0.1, 0.1, 0.85, opacity);
        Color negativeMaxColor = new Color(0.6, 0.6, 0.6, opacity);
        for (int i = 0; i < colorRange; i++) {
            materials[i] = new PhongMaterial(new Color(
                    negativeMinColor.getRed() + (i / (float) colorRange) * (negativeMaxColor.getRed() - negativeMinColor.getRed()),
                    negativeMinColor.getGreen() + (i / (float) colorRange) * (negativeMaxColor.getGreen() - negativeMinColor.getGreen()),
                    negativeMinColor.getBlue() + (i / (float) colorRange) * (negativeMaxColor.getBlue() - negativeMinColor.getBlue()),
                    opacity
            ));
        }
        for (int i = 0; i < colorRange; i++) {
            materials[i + colorRange] = new PhongMaterial(new Color(
                    positiveMinColor.getRed() + (i / (float) colorRange) * (positiveMaxColor.getRed() - positiveMinColor.getRed()),
                    positiveMinColor.getGreen() + (i / (float) colorRange) * (positiveMaxColor.getGreen() - positiveMinColor.getGreen()),
                    positiveMinColor.getBlue() + (i / (float) colorRange) * (positiveMaxColor.getBlue() - positiveMinColor.getBlue()),
                    opacity
            ));
        }
    }

    public PhongMaterial getMaterial(float value){
        //check for NaN -> https://stackoverflow.com/questions/9341653/float-nan-float-nan
        if (value != value) {
            return NaNMaterial;
        }
        int materialIndex;
        if (value > colorRange) {
            materialIndex = materials.length - 1;
        } else if (value < -colorRange) {
            materialIndex = 0;
        } else if ((int) value == 0) {
            materialIndex = value > 0 ? colorRange : colorRange - 1;
        } else {
            materialIndex = (int) value + colorRange - 1;
        }
        return materials[materialIndex];
    }

    public void prepareScaleLegend(VBox container){
        container.getChildren().clear();
        for(int i = colorRange * 2-1; i >= 0; i--){
            float size = (float)container.getHeight()/(colorRange *2)-3;
            HBox temp = new HBox();
            temp.minHeight(size);
            temp.setAlignment(Pos.CENTER);
            Text text = new Text((i-colorRange) + "");
            text.minWidth(20);
            //temp.getChildren().add(text);
            Pane pane = new Pane();
//            pane.setPrefWidth(container.getHeight()/colorRange *2);
//            pane.setPrefHeight(container.getHeight()/colorRange *2);
            pane.setMinSize(size,size);
            pane.setMaxSize(size,size);
            pane.setBackground(new Background(new BackgroundFill(
                    new Color(
                            materials[i].getDiffuseColor().getRed(),
                            materials[i].getDiffuseColor().getGreen(),
                            materials[i].getDiffuseColor().getBlue(),
                            1
                    ),CornerRadii.EMPTY, Insets.EMPTY)));
            temp.getChildren().add(pane);
            container.getChildren().add(temp);
        }
    }

    public PhongMaterial getNaNMaterial() {
        return NaNMaterial;
    }

    public PhongMaterial[] getMaterials() {
        return materials;
    }
}
