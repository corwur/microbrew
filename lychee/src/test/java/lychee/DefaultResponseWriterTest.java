package lychee;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultResponseWriterTest {

    @Mock
    private OutputStream outputStream;

    @Test
    public void write() throws IOException {
        var responseWriter = new DefaultResponseWriter();
        responseWriter.write(outputStream, "message");
        verify(outputStream).write(any());
    }

    @Test
    public void writeNull() throws IOException {
        var responseWriter = new DefaultResponseWriter();
        responseWriter.write(outputStream, null);
        verify(outputStream, never()).write(any());
    }

}