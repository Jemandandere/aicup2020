import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DebugInterface {
    private InputStream inputStream;
    private OutputStream outputStream;

    public DebugInterface(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void drawLine(Vec2Int a, Vec2Int b, Color color) {
        Vec2Float screenOffset = new Vec2Float(0, 0);
        send(
                new DebugCommand.Add(
                        new DebugData.Primitives(
                                    new ColoredVertex[]{
                                        new ColoredVertex(new Vec2Float(a.getX() + 0.5f, a.getY() + 0.5f), screenOffset, color),
                                        new ColoredVertex(new Vec2Float(b.getX() + 0.5f, b.getY() + 0.5f), screenOffset, color)
                                    },
                                PrimitiveType.LINES
                        )
                )
        );
    }

    public void send(model.DebugCommand command) {
        try {
            new model.ClientMessage.DebugMessage(command).writeTo(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public model.DebugState getState() {
        try {
            new model.ClientMessage.RequestDebugState().writeTo(outputStream);
            outputStream.flush();
            return model.DebugState.readFrom(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}