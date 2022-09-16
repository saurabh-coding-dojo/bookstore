<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page isErrorPage="true"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:navigation>

	<div class="col-sm-6">
<hr>
		<div class="card">
			<div class="card-header">
				<h3>Create a New Book</h3>
			</div>
			<div class="card-body">
				<form:form action="/books/create" method="post"
					modelAttribute="book" enctype="multipart/form-data">
					<c:if test="${not empty error}">${error}</c:if>
					<div class="form-group">
						<form:label path="title">Title</form:label>
						<form:errors class="text-danger" path="title" />
						<form:input class="form-control" path="title" />
					</div>
					<div class="form-group">
						<form:label path="author">Author</form:label>
						<form:errors class="text-danger" path="author" />
						<form:input class="form-control" path="author" />
					</div>
					<div class="form-group">
						<form:label path="year">Year</form:label>
						<form:errors class="text-danger" path="year" />
						<form:input type="number" class="form-control" path="year" />
					</div>
					<div class="form-group">
						<form:label path="genre">Genre</form:label>
						<form:errors class="text-danger" path="genre" />
						<form:input class="form-control" path="genre" />
					</div>
					<div class="form-group">
						<form:label path="description">Description</form:label>
						<form:errors class="text-danger" path="description" />
						<form:textarea class="form-control" path="description" />
					</div>
					<div class="form-group">
						<form:label path="isbn">ISBN</form:label>
						<form:errors class="text-danger" path="isbn" />
						<form:input class="form-control" path="isbn" />
					</div>
					<div class="form-group">
						<form:label path="imgfile">Add Photo</form:label>
						<form:errors class="text-danger" path="imgfile" />
						<form:input type="file"
							class="text-center center-block file-upload" path="imgfile" />
					</div>

					<hr>
					<button type="submit" class="btn btn-secondary float-end">Create</button>
				</form:form>
			</div>
		</div>

	</div>

</t:navigation>

