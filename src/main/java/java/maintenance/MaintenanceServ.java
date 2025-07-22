package maintenance;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
public class MaintenanceServ {

    public static void main(String[] args) throws IOException, InterruptedException {
        //jmDNS registration
        registerService("MaintenanceService", 50058, "_maintenance._tcp.local.", "Handles maintenance reports");

        //start of gRPC server
        Server server = ServerBuilder.forPort(50058)
                .addService(new MaintenanceServiceImpl())
                .build();

        System.out.println("MaintenanceService started on port 50058...");
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

    static class MaintenanceServiceImpl extends MaintenanceServiceGrpc.MaintenanceServiceImplBase {

        @Override
        public StreamObserver<MaintenanceRequest> manageReports(StreamObserver<MaintenanceResponse> responseObserver) {
            return new StreamObserver<MaintenanceRequest>() {
                @Override
                public void onNext(MaintenanceRequest request) {
                    System.out.println("Received report: " + request.getIssueSummary());

                    MaintenanceResponse response = MaintenanceResponse.newBuilder()
                            .setAction("Send team to " + request.getLocation())
                            .setTechnicianNote("Report " + request.getReportId() + " acknowledged.")
                            .build();

                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in stream: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}