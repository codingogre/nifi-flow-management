# NiFi Flow Management
Program to schedule a processor to run along with its process group

Required Software: Groovy http://groovy-lang.org/download.html

```
usage: nifi-flow-management.groovy <OPTIONS> <COMMAND>
Options
 -h,--help                      Usage Information
 -n,--host <hostname>           Hostname of the NiFi manager
 -p,--processor <processorId>   Processor in a process group that will be
                                scheduled to run
 -s,--start                     Start the processor group
 -t,--port <port>               Port of the NiFi manager
 
 Example: This will configure a flow with a processor ID of cbccc925-0158-1000-a8f4-1cb4e1441bc0 to start immediately
 
 groovy nifi-flow-management.groovy --processor cbccc925-0158-1000-a8f4-1cb4e1441bc0 --host myhostname --start
 ```
 You can find the Process ID for a given processor by looking at its "SETTINGS" tab of a processor's configuration.
 
 https://github.com/codingogre/nifi-flow-management/blob/master/images/NiFi%20Github%20Example%201.PNG
 
 https://github.com/codingogre/nifi-flow-management/blob/master/images/NiFi%20Github%20Example%202.PNG
 
