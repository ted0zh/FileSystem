import java.io.*;
import java.util.ArrayList;
public class ByteConverter<T> {

    public ByteConverter(){}

    public byte[] convertToBytes(T object) throws IOException
    {
        byte[] result;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = null;
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            result = bos.toByteArray();
        }
        return result;
    }

    public T convertFromBytes(ArrayList<Integer> integerArray) throws IOException, ClassNotFoundException
    {
            byte[] byteArray = new byte[integerArray.size()];
            for(int i=0; i<integerArray.size(); i++)
            {
            byteArray[i] = integerArray.get(i).byteValue();
        }
            try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
             ObjectInputStream in = new ObjectInputStream(bis))
            {
            return (T) in.readObject();
        }
    }
}
