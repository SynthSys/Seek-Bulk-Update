# Seek-Bulk-Update
Code snippets for updating all properties of a whole ISA tree, for example permissions settings

This Java application provides functionality to add or update policy objects associated with ISA entities stored in a [FAIRDOM SEEK](https://fair-dom.org/platform/seek/) repository. The main class is `ed.synthsys.bulk.update.PermissionsSetter` which contains methods that can recursively apply a specified SEEK [Policy](https://docs.seek4science.org/tech/api/index.html#section/Policy) attribute to an [Investigation](https://docs.seek4science.org/tech/api/index.html#tag/investigations) entity and all of its descendants as defined in the following list:

1. [Investigation](https://docs.seek4science.org/tech/api/index.html#tag/investigations)
2. [Study](https://docs.seek4science.org/tech/api/index.html#tag/studies)
3. [Assay](https://docs.seek4science.org/tech/api/index.html#tag/assays)
4. [Models](https://docs.seek4science.org/tech/api/index.html#tag/models)
5. [Data File](https://docs.seek4science.org/tech/api/index.html#tag/dataFiles)

Naturally, having to modify all descendants of an Investigation object manually would be extremely tedious and laborious, so this application provides a convenient means of adding permissions for a SEEK user in a batch operation.

## Dependencies

This application requires the entity classes provided by [Seek-Java-RESTClient](https://github.com/SynthSys/Seek-Java-RESTClient).

## Using the Bulk Update

A `main` method is available in the `ed.synthsys.bulk.update.PermissionsSetter` class. There are seven configuration properties that need to be specified:

```java
    /*
    * Configuration Properties
    */
    int investigationId = 8;
    int seekPersonId = 2;
    String policyAccess = "manage";
    String seekType = "person";
    String userName = "test";
    String password = "test";
    SeekRestApiClient apiClient = new SeekRestApiClient("https://fairdomhub.org/", userName, password);
    /*
    * End of Configuration Properties
    */
```
| Property          | Description and How to Specify                               |
| ----------------- | ------------------------------------------------------------ |
| `investigationId` | The SEEK ID of the Investigation resource in the repository. This can be discovered by browsing the resource URLs in the repository, for example https://fairdomhub.org/investigations/**<u>247</u>** |
| `seekPersonId`    | The SEEK ID of the Person resource in the repository for whom the new policy shall apply. For example, https://fairdomhub.org/people/**<u>1194</u>** |
| `policyAccess`    | The desired access level for the user relative to the ISA entities. The string values are specified in the [SEEK API](https://docs.seek4science.org/tech/api/index.html#section/Policy) and the options are (in order of increasing privilege):  `"no_access"`,  `"view"`, `"download"`, `"edit"`, and `"manage"` |
| `seekType`        | The type of resource in the SEEK repository that the new policy applies to. This will typically be a `"person"` ([people](https://docs.seek4science.org/tech/api/index.html#tag/people)) type, but it could be a policy associated with an `"institution"` ([institutions](https://docs.seek4science.org/tech/api/index.html#tag/institutions)), a `"programme"` ([programmes](https://docs.seek4science.org/tech/api/index.html#tag/programmes)) or a `"project"` ([projects](https://docs.seek4science.org/tech/api/index.html#tag/projects)) |
| `apiClient`       | A new `ed.synthsys.seek.SeekRestApiClient` as provided by [Seek-Java-RESTClient](https://github.com/SynthSys/Seek-Java-RESTClient). The first parameter is the String URL for the SEEK repository, typically [FAIRDOMHub](https://fairdomhub.org/) itself. The second and third parameters are the username and password for the user that has manage permissions over the ISA resources in the repository. |

Once all of these parameters have been specified, running the main method executes the permissions updates and output shall be visible in the console log.

It is also possible to update individual ISA entities by invoking any of the `update`* methods with the `recursive` argument set to `false`. 

