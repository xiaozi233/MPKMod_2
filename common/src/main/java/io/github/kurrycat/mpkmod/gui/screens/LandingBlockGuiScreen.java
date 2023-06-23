package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LandingBlockGuiScreen extends ComponentScreen {
    public static List<LandingBlock> lbs = new ArrayList<>();
    public static Color lbListColorItemEdge = new Color(255, 255, 255, 95);
    public static Color lbListColorBg = new Color(31, 31, 31, 150);

    private LBList lbList;

    public static List<Vector3D> calculateLBOffsets() {
        List<Vector3D> returnOffsets = new ArrayList<>();
        lbs.forEach(lb -> {
            Vector3D o = lb.saveOffsetIfInRange();
            if (o != null)
                returnOffsets.add(o);
        });
        return returnOffsets;
    }

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();

        lbList = new LBList(
                new Vector2D(0, 16),
                new Vector2D(3 / 5D, -40)
        );
        addChild(lbList, PERCENT.SIZE_X, Anchor.TOP_CENTER);
        lbList.addChild(
                new Button(
                        "x",
                        new Vector2D(3, -13),
                        new Vector2D(11, 11),
                        mouseButton -> close()
                ),
                PERCENT.NONE, Anchor.TOP_RIGHT
        );
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (int i = 0; i < lbList.getItemCount(); i++) {
            lbList.getItem(i).landingBlock.highlight = false;
        }
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    public static class LBList extends ScrollableList<LBListItem> {
        public LBList(Vector2D pos, Vector2D size) {
            this.setPos(pos);
            this.setSize(size);
            updateList();
            Button addLB = new Button("Add LB", new Vector2D(0, -22), new Vector2D(40, 20), mouseButton -> {
                if (Mouse.Button.LEFT.equals(mouseButton)) {
                    Vector3D lookingAt = WorldInteraction.getLookingAt();
                    if (lookingAt != null) {
                        lbs.add(new LandingBlock(new BoundingBox3D(lookingAt, lookingAt.add(1))));
                    } else if (Player.getLatest() != null) {
                        lbs.add(new LandingBlock(BoundingBox3D.asBlockPos(Player.getLatest().getPos())));
                    } else {
                        lbs.add(new LandingBlock(new BoundingBox3D(new Vector3D(0, 0, 0), new Vector3D(0, 0, 0))));
                    }
                    items.add(new LBListItem(this, lbs.get(lbs.size() - 1)));
                }
            });
            addChild(addLB, PERCENT.NONE, Anchor.BOTTOM_CENTER);

            this.title = "Landing Blocks";
        }

        public void updateList() {
            items = lbs.stream().map(lb -> new LBListItem(this, lb)).collect(Collectors.toCollection(ArrayList<LBListItem>::new));
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            for (int i = 0; i < getItemCount(); i++) {
                getItem(i).landingBlock.highlight = false;
            }
            Pair<LBListItem, Vector2D> p = getItemAndRelMousePosUnderMouse(mouse);
            if (p != null)
                p.first.landingBlock.highlight = true;

            components.forEach(c -> c.render(mouse));
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return super.handleMouseInput(state, mousePos, button);
        }
    }

    public static class LBListItem extends ScrollableListItem<LBListItem> {
        public CheckButton enabled;
        public LandingBlock landingBlock;

        public InputField minX, minY, minZ, maxX, maxY, maxZ;
        public InputField[] fields;

        public boolean collapsed = true;
        public Button collapseButton;
        public Button deleteButton;
        public Button landingModeButton;

        public LBListItem(ScrollableList<LBListItem> parent, LandingBlock landingBlock) {
            super(parent);
            this.landingBlock = landingBlock;
            enabled = new CheckButton(Vector2D.ZERO, checked -> {
                landingBlock.enabled = checked;
            });
            enabled.setChecked(landingBlock.enabled);
            minX = new InputField(String.valueOf(landingBlock.boundingBox.getMin().getX()), Vector2D.OFFSCREEN, 25, true)
                    .setName("minX: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMinX(c.getNumber());
                    });
            minY = new InputField(String.valueOf(landingBlock.boundingBox.getMin().getY()), Vector2D.OFFSCREEN, 25, true)
                    .setName("minY: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMinY(c.getNumber());
                    });
            minZ = new InputField(String.valueOf(landingBlock.boundingBox.getMin().getZ()), Vector2D.OFFSCREEN, 25, true)
                    .setName("minZ: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMinZ(c.getNumber());
                    });
            maxX = new InputField(String.valueOf(landingBlock.boundingBox.getMax().getX()), Vector2D.OFFSCREEN, 25, true)
                    .setName("maxX: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMaxX(c.getNumber());
                    });
            maxY = new InputField(String.valueOf(landingBlock.boundingBox.getMax().getY()), Vector2D.OFFSCREEN, 25, true)
                    .setName("maxY: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMaxY(c.getNumber());
                    });
            maxZ = new InputField(String.valueOf(landingBlock.boundingBox.getMax().getZ()), Vector2D.OFFSCREEN, 25, true)
                    .setName("maxZ: ")
                    .setOnContentChange(c -> {
                        if (c.getNumber() != null) landingBlock.boundingBox.setMaxZ(c.getNumber());
                    });

            fields = new InputField[]{minX, minY, minZ, maxX, maxY, maxZ};

            collapseButton = new Button("v", Vector2D.OFFSCREEN, new Vector2D(11, 11), mouseButton -> {
                if (mouseButton == Mouse.Button.LEFT) collapsed = !collapsed;
            });
            deleteButton = new Button("x", Vector2D.OFFSCREEN, new Vector2D(11, 11), mouseButton -> {
                if (mouseButton == Mouse.Button.LEFT) {
                    LandingBlockGuiScreen.lbs.remove(landingBlock);
                    parent.items.remove(this);
                }
            });
            deleteButton.textColor = Color.RED;
            deleteButton.pressedTextColor = Color.RED;

            landingModeButton = new Button("", Vector2D.OFFSCREEN, Vector2D.ZERO, mouseButton -> {
                if (mouseButton == Mouse.Button.LEFT) {
                    landingBlock.landingMode = landingBlock.landingMode.getNext();
                } else if (Mouse.Button.RIGHT.equals(mouseButton)) {
                    switch (landingBlock.landingMode) {
                        case Z_NEO:
                            landingBlock.boundingBox.setMinZ(landingBlock.boundingBox.getMin().getZ() + 0.6D);
                            landingBlock.boundingBox.setMaxZ(landingBlock.boundingBox.getMax().getZ() - 0.6D);
                            break;
                        case ENTER:
                            landingBlock.boundingBox.setMaxX((int) landingBlock.boundingBox.getMin().getX() + 0.7D);
                            landingBlock.boundingBox.setMaxZ((int) landingBlock.boundingBox.getMin().getZ() + 0.7D);
                            landingBlock.boundingBox.setMinX((int) landingBlock.boundingBox.getMin().getX() + 0.3D);
                            landingBlock.boundingBox.setMinZ((int) landingBlock.boundingBox.getMin().getZ() + 0.3D);
                            break;
                    }
                }
            });
        }

        public int getHeight() {
            return collapsed ? 21 : 50;
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, lbListColorBg, lbListColorItemEdge);

            for (int i = 0; i < fields.length; i++) {
                fields[i].setPos(pos.add(
                        size.getX() / 12 + size.getX() / 5 * 2 * (((int) (i / 3))),
                        size.getY() / 4 * (1 + (i % 3)) - fields[i].getDisplayedSize().getY() / 2
                ));
                fields[i].setWidth(size.getX() / 3);
            }

            if (collapsed)
                FontRenderer.drawString(
                        landingBlock.boundingBox.getMin() + " - " + landingBlock.boundingBox.getMax(),
                        pos.add(size.div(2))
                                .sub(0, FontRenderer.getStringSize(landingBlock.boundingBox.getMin() + " - " + landingBlock.boundingBox.getMax()).getY() / 2D - 1)
                                .sub(FontRenderer.getStringSize(landingBlock.boundingBox.getMin() + " ").getX(), 0)
                                .sub(FontRenderer.getStringSize("-").getX() / 2D, 0),
                        Color.WHITE,
                        false
                );
            else
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setPos(pos.add(
                            size.getX() / 12 + size.getX() / 5 * 2 * (((int) (i / 3))),
                            size.getY() / 4 * (1 + (i % 3)) - fields[i].getDisplayedSize().getY() / 2
                    ));
                    fields[i].setWidth(size.getX() / 3);
                    fields[i].render(mouse);
                }

            enabled.setPos(pos.add(size.getX() / 16 - enabled.getDisplayedSize().getX(), size.getY() / 2 - 5.5).round());
            enabled.render(mouse);

            collapseButton.setText(collapsed ? "v" : "^");
            collapseButton.textOffset = collapsed ? Vector2D.ZERO : new Vector2D(0, 3);
            collapseButton.setPos(pos.add(size.getX() - size.getX() / 16, size.getY() / (collapsed ? 2 : 3) - 5.5).round());
            collapseButton.render(mouse);

            deleteButton.setPos(pos.add(size.getX() - size.getX() / 8, size.getY() / (collapsed ? 2 : 3) - 5.5).round());
            deleteButton.render(mouse);

            landingModeButton.setPos(pos.add(size.getX() - size.getX() / 8, size.getY() / 3 * 2 - 5.5).round());
            landingModeButton.setSize(new Vector2D(size.getX() / 16 + collapseButton.getDisplayedSize().getX(), 11));
            landingModeButton.enabled = !collapsed;
            landingModeButton.setText(landingBlock.landingMode.toString());
            if (!collapsed) landingModeButton.render(mouse);
        }

        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return ArrayListUtil.orMapAll(
                    collapsed ? ArrayListUtil.getAllOfType(MouseInputListener.class, enabled, collapseButton, deleteButton, landingModeButton) :
                            ArrayListUtil.getAllOfType(MouseInputListener.class, minX, minY, minZ, maxX, maxY, maxZ, enabled, collapseButton, deleteButton, landingModeButton),
                    ele -> ele.handleMouseInput(state, mousePos, button)
            );
        }

        public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
            return !collapsed && ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(KeyInputListener.class, minX, minY, minZ, maxX, maxY, maxZ),
                    ele -> ele.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
            );
        }
    }
}
