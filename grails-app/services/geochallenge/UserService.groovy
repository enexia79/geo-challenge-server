package geochallenge

import grails.transaction.Transactional

@Transactional
class UserService {
	/**
	 * Returns the user id given the surrogate id which is the id of the social network used to create the
	 * user account.  I.e. Facebook Id or Google Id
	 * @param surrogateId The unqiue social Id used to create the user.
	 * @return user
	 */
    def getIdBySurrogateId(surrogateId) {
		return User.findBySurrogateId(surrogateId)?.id
    }
	
	/**
	 * Create a user account given info dictionary object
	 * @param info	Dictionary object containing the name and surrogateId of the user to create
	 * @return user	The user object is return on success, null otherwise
	 */
	def create(info) {
		if(info == null)
			return null
		
		def user = new User(info);
		if(user.save())
			return user
		else
			return null
	}
	
	/**
	 * Toggle user active or not
	 * @param User User id or User object to toggle
	 * @return boolean True if successful.
	 */
	def toggleActive(user) {
		if(user == null)
			return false
			
		if(user instanceof Integer || user instanceof Long)
			user = User.get(user)
			
		if(user instanceof User)
		{
			user.active = !user.active
			if(user.save())
				return true
		}
		
		return false
	}
}
