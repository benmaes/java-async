package be.bewire;

import be.bewire.resources.AsyncTvResource;
import be.bewire.resources.BasicResource;
import be.bewire.resources.SyncTvResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MyApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(BasicResource.class);
		classes.add(SyncTvResource.class);
		classes.add(AsyncTvResource.class);
		return classes;
	}
}