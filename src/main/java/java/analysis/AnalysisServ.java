package analysis;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
public class AnalysisServ {
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

    public static void main(String[] args) throws IOException, InterruptedException {
        //jmDNS registration
        registerService("AnalysisService", 50057, "_analysis._tcp.local.", "Analyzes sensor data");

        //start of gRPC server
        Server server = ServerBuilder.forPort(50057)
                .addService(new AnalysisServiceImpl())
                .build();

        System.out.println("AnalysisService started on port 50057...");
        server.start();
        server.awaitTermination();
    }

    static class AnalysisServiceImpl extends AnalysisServiceGrpc.AnalysisServiceImplBase {
        @Override
        public void analyzeSensorData(SensorData request, StreamObserver<AnalysisResult> responseObserver) {
            String status;
            String message;

            if (request.getTemperature() > 28 || request.getVibration() > 4) {
                status = "WARNING";
                message = "Abnormal temperature or vibration detected";
            } else {
                status = "OK";
                message = "Sensor readings are in normal rang";
            }

            AnalysisResult result = AnalysisResult.newBuilder()
                    .setStatus(status)
                    .setMessage(message)
                    .build();

            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }
    }
}