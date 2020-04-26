package com.example.pt.controller;

import com.example.pt.domain.Circle;
import com.example.pt.domain.Point;
import com.example.pt.util.PlotUtils;
import com.example.pt.util.StateResolverUtils;
import com.example.pt.util.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Route("")
@Slf4j
public class Lab2Route extends VerticalLayout {
    private static final String IMAGE_ID = "circleGraphId";

    // Константные имена для полей и кнопок
    private static final String CLEAR_STATE_BUTTON_NAME = "Clear state";
    private static final String APPLY_NAME_BUTTON = "Apply";
    private static final String nameRadiusLayoutIfEmpty = "Radius:";
    private static final String nameRadiusLayoutIfNotEmpty = "Radius multiplier:";
    private static final String nameXLayoutIfEmpty = "Center x:";
    private static final String nameXLayoutIfNotEmpty = "Movement x:";
    private static final String nameYLayoutIfEmpty = "Center y:";
    private static final String nameYLayoutIfNotEmpty = "Movement y:";

    private static boolean isAvailableState() {
        return StateResolverUtils.deserializeFromFile() != null;
    }

    private static final Supplier<String> radiusProducer = () -> isAvailableState() ? nameRadiusLayoutIfNotEmpty : nameRadiusLayoutIfEmpty;
    private static final Supplier<String> xProducer = () -> isAvailableState() ? nameXLayoutIfNotEmpty : nameXLayoutIfEmpty;
    private static final Supplier<String> yProducer = () -> isAvailableState() ? nameYLayoutIfNotEmpty : nameYLayoutIfEmpty;


    private static final Map<String, Supplier<String>> graphNameToProducers = new HashMap<String, Supplier<String>>() {{
        put(nameRadiusLayoutIfEmpty, radiusProducer);
        put(nameRadiusLayoutIfNotEmpty, radiusProducer);
        put(nameXLayoutIfEmpty, xProducer);
        put(nameXLayoutIfNotEmpty, xProducer);
        put(nameYLayoutIfEmpty, yProducer);
        put(nameYLayoutIfNotEmpty, yProducer);
    }};

    private static final Consumer<Label> resolveNewNameForLabel = label -> {
        String currentLabelName = label.getText();
        Supplier<String> newNameForLabelProducer = graphNameToProducers.get(currentLabelName);
        String newLabelName = newNameForLabelProducer.get();
        label.setText(newLabelName);
    };


    private static final BiFunction<Double, Double, Double> multipleResultProducer = (t1, t2) -> t1 * t2;
    private static final BiFunction<Double, Double, Double> foldResultProducer = Double::sum;


    public Lab2Route() {
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        //Инициализация лейбла и поля для радиуса
        HorizontalLayout horizontalLayoutForRadius = new HorizontalLayout();
        Label radiusLabel = new Label(radiusProducer.get());
        NumberField radiusNumberField = new NumberField();
        radiusNumberField.setStep(Double.MIN_NORMAL);
        radiusNumberField.setId(radiusLabel.getText());
        radiusNumberField.setMin(0.00001);
        radiusLabel.setFor(radiusNumberField);

        horizontalLayoutForRadius.add(radiusLabel, radiusNumberField);

        //Инициализация лейбла и поля для x координаты
        HorizontalLayout horizontalLayoutForXCoordinate = new HorizontalLayout();
        Label xLabel = new Label(xProducer.get());
        NumberField xNumberField = new NumberField();
        xNumberField.setStep(Double.MIN_NORMAL);
        xNumberField.setId(xLabel.getText());
        xLabel.setFor(xNumberField);

        horizontalLayoutForXCoordinate.add(xLabel, xNumberField);


        //Инициализация лейбла и поля для y координаты
        HorizontalLayout horizontalLayoutForYCoordinate = new HorizontalLayout();
        Label yLabel = new Label(yProducer.get());
        NumberField yNumberField = new NumberField();
        yNumberField.setStep(Double.MIN_NORMAL);
        yNumberField.setId(yLabel.getText());
        yLabel.setFor(yNumberField);

        horizontalLayoutForYCoordinate.add(yLabel, yNumberField);

        //Инициализация кнопки apply
        HorizontalLayout horizontalLayoutForButtonsApplyAndRestore = new HorizontalLayout();
        Button applyButton = new Button();
        applyButton.setText(APPLY_NAME_BUTTON);
        applyButton.addClickListener(clickEvent -> {
            Circle circle = StateResolverUtils.deserializeFromFile();
            Double radiusEnteredValue = radiusNumberField.getValue();
            Double xEnteredValue = xNumberField.getValue();
            Double yEnteredValue = yNumberField.getValue();
            List<String> errorMessages = new LinkedList<>();
            if (Objects.isNull(radiusEnteredValue) && (Objects.isNull(circle) || circle.getR() == 0)) {
                errorMessages.add("You can not enter the empty radius");
            }
            if (Objects.isNull(xEnteredValue) && (Objects.isNull(circle) || Objects.isNull(circle.getCenter()) || circle.getCenter().getX() == 0)) {
                errorMessages.add("You can not enter the empty x");
            }
            if (Objects.isNull(yEnteredValue) && (Objects.isNull(circle) || Objects.isNull(circle.getCenter()) || circle.getCenter().getY() == 0)) {
                errorMessages.add("You can not enter the empty y");
            }
            if (!CollectionUtils.isEmpty(errorMessages)) {
                errorMessages.forEach(message -> Notification.show(message, 5000, Notification.Position.TOP_END));
            } else {
                if (Objects.isNull(circle)) {
                    circle = new Circle(new Point(xEnteredValue, yEnteredValue), radiusEnteredValue);
                    StateResolverUtils.serializeToFileState(circle);
                } else {
                    Point center = circle.getCenter();
                    Double currentYValue = center.getY();
                    Double currentXValue = center.getX();
                    Double currentRadiusValue = circle.getR();

                    Double radiusNewValue = calculateValue(currentRadiusValue, radiusEnteredValue, multipleResultProducer);
                    Double xNewValue = calculateValue(currentXValue, xEnteredValue, foldResultProducer);
                    Double yNewValue = calculateValue(currentYValue, yEnteredValue, foldResultProducer);

                    Circle obtainedCircle = new Circle(new Point(xNewValue, yNewValue), radiusNewValue);

                    StateResolverUtils.serializeToFileState(obtainedCircle);
                }
                showImage();
                refreshNameForLabels(radiusLabel, xLabel, yLabel);
                restoreNumberFields(radiusNumberField, xNumberField, yNumberField);
            }
        });

        //Инициализация кнопки clear state
        Button clearButton = new Button();
        clearButton.setText(CLEAR_STATE_BUTTON_NAME);
        clearButton.addClickListener(clickEvent -> {
            try {
                StateResolverUtils.clearState();
                showImage();
                refreshNameForLabels(radiusLabel, xLabel, yLabel);
            } catch (IOException e) {
                log.error("Error during clear state", e);
            }
        });

        horizontalLayoutForButtonsApplyAndRestore.add(applyButton, clearButton);

        add(horizontalLayoutForRadius, horizontalLayoutForXCoordinate, horizontalLayoutForYCoordinate, horizontalLayoutForButtonsApplyAndRestore);

        //Показываем картинку только если состояние можно десериализовать
        if (isAvailableState()) {
            showImage();
            refreshNameForLabels(radiusLabel, xLabel, yLabel);
        }
    }

    private Double calculateValue(Double currentValue, Double operatorValue, BiFunction<Double, Double, Double> resultProducer) {
        if (Objects.isNull(operatorValue)) {
            if (Objects.isNull(currentValue)) {
                throw new RuntimeException(); //Эта ситуация никогда невозможна, если возможна то только захачив
            }
            return currentValue;
        }
        if (Objects.isNull(currentValue)) {
            return operatorValue;
        }

        return resultProducer.apply(currentValue, operatorValue);
    }

    private void restoreNumberFields(NumberField... numberFields) {
        for (NumberField numberField : numberFields) {
            numberField.setValue(null);
        }
    }

    private void refreshNameForLabels(Label... labels) {
        for (Label label : labels) {
            resolveNewNameForLabel.accept(label);
        }
    }

    private void showImage() {
        VaadinImageWrapper imageWrapper = new VaadinImageWrapper();
        Optional<VaadinImageWrapper> plottedGraph = imageWrapper.plotGraph();
        plottedGraph.ifPresent(VaadinImageWrapper::addOrRefresh);
    }

    private class VaadinImageWrapper {
        private Image image;

        public Optional<VaadinImageWrapper> plotGraph() {
            Circle initCircle = StateResolverUtils.deserializeFromFile();
            if (Objects.isNull(initCircle)) {
                removeIfExists();
                return Optional.empty();
            }
            BufferedImage bufferedImage = PlotUtils.plotCircle(initCircle);
            return plotGraphInternal(bufferedImage);
        }

        private Optional<VaadinImageWrapper> plotGraphInternal(BufferedImage bufferedImage) {
            StreamResource streamResource = VaadinUtils.streamResource(bufferedImage);
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            removeIfExists();
            image = new Image(streamResource, "");

            image.setId(IMAGE_ID);
            image.setWidth(String.format("%spx", String.valueOf(width)));
            image.setHeight(String.format("%spx", String.valueOf(height)));
            return Optional.of(this);
        }

        private void removeIfExists() {
            Optional<Component> imageIfExist = getImageIfExist();
            imageIfExist.ifPresent(Lab2Route.this::remove);
        }

        private void addOrRefresh() {
            add(image);
        }

        private Optional<Component> getImageIfExist() {
            return getChildren().filter(component -> component.getId().isPresent() && component.getId().get().equals(IMAGE_ID)).findFirst();
        }
    }
}
