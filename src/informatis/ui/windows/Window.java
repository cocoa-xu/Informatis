package informatis.ui.windows;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class Window extends Table {
    public final TextureRegionDrawable icon;
    @Nullable
    public final Cons<Table> content;
    public boolean shown = false;

    public float minWindowWidth = 160, minWindowHeight = 60;
    public static float maxWindowWidth = Float.MAX_VALUE, maxWindowHeight = Float.MAX_VALUE;

    public Window(String name){
        this(new TextureRegionDrawable(Core.atlas.find("clear")), name, null);
    }
    public Window(TextureRegionDrawable icon, String name){
        this(icon, name, null);
    }
    public Window(TextureRegionDrawable icon, String name, Cons<Table> content) {
        this.icon = icon;
        this.name = name;
        this.content = content;
    }

    public void build() {
        table(t -> {
            t.table(Tex.buttonEdge1, b -> {
                b.left();
                b.image(icon.getRegion()).scaling(Scaling.fill).size(20f);
                b.add(Core.bundle.get("window." + name + ".name")).padLeft(20);
            }).grow();
            t.table(Tex.buttonEdge3, b ->
                b.button(Icon.cancel, Styles.emptyi, () -> shown = false).grow()
            ).maxWidth(8 * 15f).growY();

            t.touchable = Touchable.enabled;
            t.addListener(new DragHandleListener(this));
        }).height(8 * 6f).growX();
        row();
        pane(new Table(t -> {
            t.setBackground(Styles.black5);
            buildBody(t);
        })).grow();
        row();
        table(Styles.black5, t -> {
            t.right();
            t.image(Icon.resizeSmall).size(20f).get().addListener(new ScaleInputListener(this));
        }).height(8 * 2f).growX();

        visible(() -> shown);
        update(() ->
            setPosition(
                Mathf.clamp(x, 0, Core.graphics.getWidth() - getWidth()),
                Mathf.clamp(y, 0, Core.graphics.getHeight() - getHeight())
            )
        );
    }

    protected void buildBody(Table t){
        if(content != null) content.get(t);
    }

    public void toggle(){
        shown = !shown;
    }

    private static class TouchPosInputListener extends InputListener {
        protected float lastX, lastY;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
            lastX = v.x;
            lastY = v.y;
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer) {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            lastX = v.x;
            lastY = v.y;
        }
    }

    private static class DragHandleListener extends TouchPosInputListener {
        Window targetWindow;
        public DragHandleListener(Window targetWindow) {
            this.targetWindow = targetWindow;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
            lastX = v.x;
            lastY = v.y;
            targetWindow.toFront();
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer) {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            targetWindow.setPosition( targetWindow.x + (v.x - lastX),  targetWindow.y + (v.y - lastY));
            lastX = v.x;
            lastY = v.y;
        }
    }

    private static class ScaleInputListener extends TouchPosInputListener {
        Window targetWindow;
        public ScaleInputListener(Window targetWindow) {
            this.targetWindow = targetWindow;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer) {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            float w = v.x - lastX;
            float h = v.y - lastY;

            // will soft-lock if initial size is smaller than minimum
            // so don't do that!
            if(targetWindow.getWidth() + w < targetWindow.minWindowWidth || targetWindow.getWidth() + w > Window.maxWindowWidth) w = 0;
            if(targetWindow.getHeight() - h < targetWindow.minWindowHeight || targetWindow.getHeight() - h > Window.maxWindowHeight) h = 0;
            targetWindow.sizeBy(w, -h);
            targetWindow.moveBy(0, h);
            lastX = v.x;
            lastY = v.y;
        }
    }
}