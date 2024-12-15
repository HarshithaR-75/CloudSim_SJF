package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * Implementation of SJF Scheduling Algorithm in CloudSim.
 */
public class sjf4 {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vmlist. */
    private static List<Vm> vmlist;

    public static void main(String[] args) {

        Log.printLine("Starting sjf4...");

        try {
            // Step 1: Initialize CloudSim
            int num_user = 1; // Number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // Trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Step 2: Create Datacenter
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            // Step 3: Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Step 4: Create Virtual Machines
            vmlist = new ArrayList<>();

            int vmid = 0;
            int mips = 250;
            long size = 10000; // Image size (MB)
            int ram = 512; // VM memory (MB)
            long bw = 1000;
            int pesNumber = 1; // Number of CPUs
            String vmm = "Xen"; // VMM name

            Vm vm1 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmid++;
            Vm vm2 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            vmlist.add(vm1);
            vmlist.add(vm2);

            broker.submitVmList(vmlist);

            // Step 5: Create Cloudlets
            cloudletList = new ArrayList<>();

            int id = 0;
            long[] cloudletLengths = { 100000, 250000 }; // Example cloudlet lengths
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            for (long length : cloudletLengths) {
                Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel,
                        utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
                id++;
            }

            // Step 6: Sort Cloudlets by Length (SJF)
            Collections.sort(cloudletList, Comparator.comparingLong(Cloudlet::getCloudletLength));

            // Step 7: Submit Cloudlets to Broker
            broker.submitCloudletList(cloudletList);

            // Bind Cloudlets to VMs in a round-robin manner
            int vmIndex = 0;
            for (Cloudlet cloudlet : cloudletList) {
                broker.bindCloudletToVm(cloudlet.getCloudletId(), vmlist.get(vmIndex).getId());
                vmIndex = (vmIndex + 1) % vmlist.size();
            }

            // Step 8: Start Simulation
            CloudSim.startSimulation();

            // Step 9: Print Results
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("sjf4 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        int mips = 1000;

        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // PE ID and MIPS rating

        int hostId = 0;
        int ram = 2048; // Host memory (MB)
        long storage = 1000000; // Host storage
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
                + indent + "Start Time" + indent + "Finish Time" + indent + "Turn Around Time" + indent + "Waiting Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                double finishTime = cloudlet.getFinishTime();
                double turnAroundTime = finishTime - cloudlet.getExecStartTime();
                double waitingTime = turnAroundTime - cloudlet.getActualCPUTime();

                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(finishTime) + indent + indent + dft.format(turnAroundTime)
                        + indent + indent + dft.format(waitingTime));
            }
        }
    }
}
