## Individual Lead Initiative Update 

### Completed
+ Volunteer verification flow
+ Initiative creation
+ Initiative update
+ Refactored codebase to be compatible with new types
+ Initiative update
+ Creation of resources
+ Created a wrapper class for project ownership 


### In progress
+ Initiative application
  + Only verified volunteers can apply 
  + Volunteers have to specify what kind of resource they will be volunteering
  + Do services count towards the total number of requested volunteers?

### 16/11/26
+ Update initiative initiativeApplicationService to create and update applications
+ Stopped setting the organization during wallet creation 
+ Updated to using creator wrapper

Pending
+ Update all organization related gets to using a left join for backwards compatibility
+ Expose endpoints to create, and modify applications
+ Expose an endpoint for volunteer to view their donation and modify them 