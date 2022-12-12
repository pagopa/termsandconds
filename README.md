----
Once you have created your repo, replace everywhere:
- <code>\_\_service_name\_\_description\_\_</code> with the short description of yout service;
- <code>\_\_service_name\_\_common_version\_\_</code> with the version of <code>mil-common</code> library that you want to use;
- <code>\_\_service_name\_\_module_id\_\_</code> with 3 digits unique module ID;
- <code>\_\_service_name\_\_</code> with the name of your service.
----

# Multi-channel Integration Layer __service_name__
__service_name__description__

## Dependencies
This project depends on <code>mil-common</code>. If you don't want to clone and install it locally, run ```mvn validate -Dmaven.home=<path to Maven home>``` before.