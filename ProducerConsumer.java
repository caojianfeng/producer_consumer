import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import static java.lang.System.out;

public class ProducerConsumer{

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
    
    final Semaphore empty = new Semaphore(2);
    final Semaphore full = new Semaphore(0);
    final Semaphore ioLock = new Semaphore(1);
    final ArrayList<Product> buffer = new ArrayList<>();
    
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
    ProductBuffer buffer = new ProductBuffer();
    
    List<Thread> tlist = new ArrayList<Thread>();
    tlist.add(new Producer("P1",buffer));
    tlist.add(new Producer("P2",buffer));
    tlist.add(new Producer("P3",buffer));
    tlist.add(new Consumer("C1",buffer));
    tlist.add(new Consumer("C2",buffer));
    for (Thread t : tlist){
      t.start();
    }
    
    out.println("Hello PC");
  }
}