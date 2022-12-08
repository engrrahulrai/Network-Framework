import com.rahul.rai.nframework.server.*;
import com.rahul.rai.nframework.server.annotations.*;
@Path("/Bank")
public class Bank
{
@Path("/getBranchName")
public String getBranchName(String d)
{
if(d.equals("hii")) return "Indra Nagar";
if(d.equals("hello")) return "Suraj Nagar";
return "No branch";
}
@Path("/getNumber")
public int getNumber(int num)
{
return num;
}
public static void main(String gg[])
{
try
{
NFrameworkServer nframeworkServer=new NFrameworkServer();
nframeworkServer.registerClass(Bank.class);
nframeworkServer.start();
}catch(Exception e)
{
System.out.println(e);
}
}
}