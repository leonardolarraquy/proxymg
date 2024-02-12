/*
 * Created on Oct 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.mg;
import com.mindergy.mg.interfaces.IFactory;


/**
 * This class is a factory for Proxy Out objects.
 * @author TIMwe
 *
 */
public class XConnSMSProxyOutFactory implements IFactory 
{
  /**
 * Simple constructor
 */
public XConnSMSProxyOutFactory()
  {
  }

  /**
 * Empty init section
 */
public void init()
  {
  }

  /** Empty object initializer 
 * @param p0 -> Parameter
 */
public void init(Object p0)
  {
  }

  /** Creates an instance of the XConnSMSProxyOut() object
 * @return -> XConnSMSProxyOut() object
 */
public Object getInstance()
  {
    return new XConnSMSProxyOut();
  }

  /** 
 * @param p0
 * @return
 */
public Object getInstance(Object p0)
  {
    return null;
  }
}