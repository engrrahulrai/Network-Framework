package com.rahul.rai.nframework.server;
import java.io.*;
import java.net.*;
import com.rahul.rai.nframework.common.*;
import java.nio.charset.*;
import java.lang.reflect.*;
class RequestProcessor extends Thread
{
private NFrameworkServer server;
private Socket socket;
RequestProcessor(NFrameworkServer server,Socket socket)
{
this.server=server;
this.socket=socket;
start();
}
public void run()
{
try
{
OutputStream os=socket.getOutputStream();
InputStream is=socket.getInputStream();
byte[] header=new byte[1024];
byte[] tmp=new byte[1024];
int i,j,k;
j=0;
int bytesToReceive=1024;
int bytesReadCount;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
j=1023;
i=1;
int requestLength=0;
while(j>=0)
{
requestLength=requestLength+((header[j]*i));
i=i*10;
j--;
}

byte [] ack=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();
byte [] requestBytes=new byte[requestLength];
bytesToReceive=requestLength;
j=0;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
requestBytes[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

String requestJSONString=new String(requestBytes,StandardCharsets.UTF_8);
Request request=JSONUtil.fromJSON(requestJSONString,Request.class);
String path=request.getServicePath();
TCPService tcpService=this.server.getTCPService(path);
Response response=new Response();
if(tcpService==null)
{
response.setSuccess(false);
response.setResult(null);
response.setException(new RuntimeException("Invalid path : "+path));
}
else
{
Class c=tcpService.c;
Method m=tcpService.method;
try
{
Object serviceObject=c.newInstance();
Object result=m.invoke(serviceObject,request.getArguments());
response.setSuccess(true);
response.setResult(result);
response.setException(null);
}catch(InstantiationException ie)
{
System.out.println("Instantiation Exception");
response.setSuccess(false);
response.setResult(null);
response.setException(new RuntimeException("Invalid path : "+path));
}
catch(IllegalAccessException iae)
{
System.out.println("Illegal Exception got raised");
response.setSuccess(false);
response.setResult(null);
response.setException(new RuntimeException("Invalid path : "+path));
}
catch(InvocationTargetException ite)
{
System.out.println("Invocation Target Exception got raised");
response.setSuccess(false);
response.setResult(null);
Throwable t=ite.getCause();
response.setException(new Exception(t.getMessage()));
}
}
String responseJSONString=JSONUtil.toJSON(response);
byte [] responseBytes=responseJSONString.getBytes(StandardCharsets.UTF_8);

header=new byte[1024];
int responseLength=responseBytes.length;
int x=responseLength;
j=1023;
while(x>0)
{
header[j]=(byte)(x%10);
x=x/10;
j--;
}

os.write(header,0,1024);
os.flush();

while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}

int bytesToSend=responseLength;
int chunkSize=1024;
j=0;
while(j<bytesToSend)
{
if(chunkSize>(bytesToSend-j)) chunkSize=(bytesToSend-j);
os.write(responseBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
socket.close();
}catch(IOException e)
{
System.out.println(e);
}
}
}