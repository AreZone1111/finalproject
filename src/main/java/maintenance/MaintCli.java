package maintenance;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.Arrays;
import java.util.List;
public class MaintCli {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50058)
                .usePlaintext()
                .build();

        MaintenanceServiceGrpc.MaintenanceServiceStub stub = MaintenanceServiceGrpc.newStub(channel);

        StreamObserver<MaintenanceRequest> requestObserver = stub.manageReports(new StreamObserver<MaintenanceResponse>() {
            @Override
            public void onNext(MaintenanceResponse response) {
                System.out.println("Received response:");
                System.out.println("Action: " + response.getAction());
                System.out.println("Note: " + response.getTechnicianNote());
                System.out.println("---------------------"); //added line breaker for better visual
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error response: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Report stream done.");
                channel.shutdown();
            }
        });

        //sending multiple reports at once
        List<MaintenanceRequest> reports = Arrays.asList(
                MaintenanceRequest.newBuilder()
                        .setReportId("1")
                        .setIssueSummary("Vibration is to high")
                        .setLocation("Bridge")
                        .build(),
                MaintenanceRequest.newBuilder()
                        .setReportId("2")
                        .setIssueSummary("Temperature is too high")
                        .setLocation("Road")
                        .build(),
                MaintenanceRequest.newBuilder()
                        .setReportId("3")
                        .setIssueSummary("Pressure is to high")
                        .setLocation("Road")
                        .build()
        );

        for (MaintenanceRequest report : reports) {
            requestObserver.onNext(report);
            try {
                Thread.sleep(1000); // this is delay between reports so they dont overlap
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        requestObserver.onCompleted();
    }
}
