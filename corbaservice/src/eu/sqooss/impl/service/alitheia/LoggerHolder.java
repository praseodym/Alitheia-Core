package eu.sqooss.impl.service.alitheia;

/**
* eu/sqooss/impl/service/alitheia/LoggerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from Alitheia.idl
* den 13 december 2007 kl 17:54 CET
*/

public final class LoggerHolder implements org.omg.CORBA.portable.Streamable
{
  public eu.sqooss.impl.service.alitheia.Logger value = null;

  public LoggerHolder ()
  {
  }

  public LoggerHolder (eu.sqooss.impl.service.alitheia.Logger initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = eu.sqooss.impl.service.alitheia.LoggerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    eu.sqooss.impl.service.alitheia.LoggerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return eu.sqooss.impl.service.alitheia.LoggerHelper.type ();
  }

}
