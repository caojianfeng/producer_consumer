import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import static java.lang.System.out;

public class ProducerConsumer{
  private static final int MAX_BUFFER_LEN = 10;
  private static final int PRODUCER_COUNT = 5;
  private static final int CONSUMER_COUNT = 3;

  static class Product{
    private String name;
    private String createBy;

    Product(String name, String createBy){
      this.name = name;
      this.createBy = createBy;
    }

    public String toString(){
      return "["+name+"] create by "+createBy;
    }
  }

  static class ProductBuffer{
    
    Semaphore empty = null;
    final Semaphore full = new Semaphore(0);
    final Semaphore ioLock = new Semaphore(1);
    final ArrayList<Product> buffer = new ArrayList<>();
    
    ProductBuffer(int bufferLen){
      this.empty = new Semaphore(bufferLen);
    }
    public void put(Product product) throws InterruptedException {
      empty.acquire();
      ioLock.acquire();
      
      buffer.add(product);
      out.println("put to buffer:"+product.toString());

      ioLock.release();
      full.release();
    }

    public Product get() throws InterruptedException {
      full.acquire();
      ioLock.acquire();
      
      Product p =  this.buffer.remove(0);
      out.println("get from buffer:"+p.toString());
      
      ioLock.release();
      empty.release();
      return p;
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
          out.println("Producer(" + name + ") error "+ e);
          break;
        }
        index++;
        out.println("Producer(" + name + ") run "+ index);
      }
      
    }
  }

  static class Consumer extends Thread{
    
    private String name;
    private ProductBuffer buffer;

    Consumer(String name,ProductBuffer buffer){
      this.name = name;
      this.buffer = buffer;
    }

    public void run(){
      
      while(true){
        try{
          Product p = this.buffer.get();
          out.println("Consumer(" + name + ") run "+ p.toString());
        }catch(Exception e){
          out.println("Consumer(" + name + ") error "+ e);
          break;
        }
      }
      
    }
  }

  public static void main(String[] args){

    ProductBuffer buffer = new ProductBuffer(MAX_BUFFER_LEN);
    
    List<Thread> tlist = new ArrayList<Thread>();
    for(int i = 0; i < PRODUCER_COUNT; i++){
      tlist.add(new Producer("P"+i,buffer));
    }

    for(int j = 0;j < CONSUMER_COUNT; j++){
      tlist.add(new Consumer("C"+j,buffer));  
    }
    
    for (Thread t : tlist){
      t.start();
    }
    
    out.println("Hello PC");
  }
}