
db = connect( 'mongodb://localhost/mil' );

db.termsconds.insertMany([
			{ 
				_id: 'XYZ13243XXYYZZ',
				version: {
							version: '1'
						 }
			},
			{ 
				_id: 'YYY13243XXYYZZ',
				version: {
							version: '2'
						 }
			}
		])
		
db.termscondsversion.insertOne({ _id: "tcVersion",	version: "1"})

printjson( db.termsconds.find( {} ) );
