package dk.magenta.datafordeler.core;

import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.servicelist.ServiceListGeneratorServlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

/**
 * Created by jubk on 01-07-2017.
 */
public class RootMatchingCXFServlet extends CXFServlet{

  protected DestinationRegistry destinationRegistry;

  @Override
  protected DestinationRegistry getDestinationRegistryFromBus() {
    this.destinationRegistry = super.getDestinationRegistryFromBus();
    return this.destinationRegistry;
  }

  @Override
  protected ServletController createServletController(ServletConfig servletConfig) {
    HttpServlet serviceListGeneratorServlet =
        new ServiceListGeneratorServlet(destinationRegistry, bus);
    ServletController newController =
        new RootMatchingServletController(destinationRegistry,
            servletConfig,
            serviceListGeneratorServlet);
    return newController;
  }
}
