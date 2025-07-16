package sensor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
public class SensServ {

    public static void main(String[] args) throws IOException, InterruptedException {
        //jmDNS registration
        registerService("SensorService", 50056, "_sensor._tcp.local.", "Streaming sensor data");

        //start of gRPC server
        Server server = ServerBuilder.forPort(50056)
                .addService(new SensorServiceImpl())
                .build();

        System.out.println("SensorService has started on port 50056");
        server.start();
        server.awaitTermination();
    }

    private static void registerService(String name, int port, String type, String description) {
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            ServiceInfo serviceInfo = ServiceInfo.create(type, name, port, description);
            jmdns.registerService(serviceInfo);
            System.out.println(name + " registered with jmDNS, on port " + port);
        } catch (Exception e) {
            System.err.println("registration failed: " + e.getMessage());
        }
    }

    static class SensorServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {
        @Override
        public void sensorStreamData(SensorRequest request, StreamObserver<SensorData> responseObserver) {
            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

            for (int i = 0; i < 10; i++) {
                SensorData data = SensorData.newBuilder()
                        .setTemperature(20 + random.nextDouble() * 10)
                        .setPressure(1000 + random.nextDouble() * 50)
                        .setVibration(random.nextDouble() * 5)
                        .setTimestamp(LocalDateTime.now().format(formatter))
                        .build();

                responseObserver.onNext(data);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            responseObserver.onCompleted();
        }
    }
}