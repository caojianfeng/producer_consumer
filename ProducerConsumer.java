import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import static java.lang.System.out;

public class ProducerConsumer{

  static class Product{
    private String name;
    private String createBy;

    Product(String name ,String createBy){
      this.name = name;
      this.createBy = createBy;
    }

    public String toString(){
      return "["+name+"] create by "+createBy;
    }
  }

  static class ProductBuffer{
    final Semaphore empty = new Semaphore(2);
    final Semaphore full = new Semaphore(0);
    final Semaphore ioLock = new Semaphore(1);
    final ArrayList<Product> buffer = new ArrayList<>();
    public void put(Product product) throws InterruptedException {
      empty.acquire();
      buffer.add(product);
      out.println("put to buffer:"+product.toString());
    }
  }

  static class Producer  extends Thread{
    
    private String name;
    private ProductBuffer buffer;

    Producer(String name,ProductBuffer buffer){
      this.name = name;
      this.buffer = buffer;
    }
    
    public void run(){
      
      int index = 0;
      while(true){
        try{
          Product p = new Product("p(" + index + ")", this.name);
          this.buffer.put(p);
        }catch(Exception e){
          out.println("Producer error "+ e);
          break;
        }
        index++;
        out.println("Producer run "+ index);
      }
      
    }
  }

  public static void main(String[] args){
    ProductBuffer buffer = new ProductBuffer();
    Producer p = new Producer("P1",buffer);
    p.start();
    out.println("Hello PC");
  }
}