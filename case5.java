package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create
 * two datacenters with one host each and
 * run cloudlets of two users on them.
 */
public class sjf5 {

    /** The cloudlet lists. */
    private static List<Cloudlet> cloudletList1;
    private static List<Cloudlet> cloudletList2;

    /** The vmlists. */
    private static List<Vm> vmlist1;
    private static List<Vm> vmlist2;

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {

        Log.printLine("Starting sjf5...");

        try {
        	//Case 5:
        	//2 users executing the same cloudlet on different data center with one host each on the virtual machine.
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 2;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            @SuppressWarnings("unused")
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            // Third step: Create Brokers
            DatacenterBroker broker1 = createBroker(1);
            int brokerId1 = broker1.getId();

            DatacenterBroker broker2 = createBroker(2);
            int brokerId2 = broker2.getId();

            // Fourth step: Create one virtual machine for each broker/user
            vmlist1 = new ArrayList<Vm>();
            vmlist2 = new ArrayList<Vm>();

            // VM description
            int vmid = 0;
            int mips = 250;
            long size = 10000; // image size (MB)
            int ram = 512; // vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; // number of cpus
            String vmm = "Xen"; // VMM name

            // create two VMs: the first one belongs to user1
            Vm vm1 = new Vm(vmid, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            // the second VM: this one belongs to user2
            Vm vm2 = new Vm(vmid, brokerId2, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            // add the VMs to the vmlists
            vmlist1.add(vm1);
            vmlist2.add(vm2);

            // submit vm list to the broker
            broker1.submitVmList(vmlist1);
            broker2.submitVmList(vmlist2);

            // Fifth step: Create two Cloudlets
            cloudletList1 = new ArrayList<Cloudlet>();
            cloudletList2 = new ArrayList<Cloudlet>();

            // Cloudlet properties
            int id = 0;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(brokerId1);

            Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet2.setUserId(brokerId2);

            // add the cloudlets to the lists: each cloudlet belongs to one user
            cloudletList1.add(cloudlet1);
            cloudletList2.add(cloudlet2);

            // submit cloudlet list to the brokers
            broker1.submitCloudletList(cloudletList1);
            broker2.submitCloudletList(cloudletList2);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
            List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

            CloudSim.stopSimulation();

            Log.print("=============> User " + brokerId1 + "    ");
            printCloudletList(newList1);

            Log.print("=============> User " + brokerId2 + "    ");
            printCloudletList(newList2);

            Log.printLine("sjf5 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<Host>();

        // A Machine contains one or more PEs or CPUs/Cores.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        // Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        // Create Host with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        // in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
        // is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerSpaceShared(peList)
                )
        ); // This is our first machine

        // Create a DatacenterCharacteristics object that stores the properties of a data center
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.001;   // the cost of using storage in this resource
        double costPerBw = 0.0;          // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(int id) {

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker" + id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Turn Around Time" + indent + "Waiting Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                double finishTime = cloudlet.getFinishTime();
                double turnAroundTime = finishTime - cloudlet.getSubmissionTime();
                double waitingTime = turnAroundTime - cloudlet.getActualCPUTime();

                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(finishTime) + indent + indent + dft.format(turnAroundTime) + indent + indent + dft.format(waitingTime));
            }
        }
    }
}
